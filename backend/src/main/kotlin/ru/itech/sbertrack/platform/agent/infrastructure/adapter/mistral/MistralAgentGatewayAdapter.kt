package ru.itech.sbertrack.platform.agent.infrastructure.adapter.mistral

import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import ru.itech.sbertrack.platform.agent.domain.model.AgentDefinition
import ru.itech.sbertrack.platform.agent.domain.model.AgentMessage
import ru.itech.sbertrack.platform.agent.domain.model.AgentMessageRole
import ru.itech.sbertrack.platform.agent.domain.port.AgentDataPort
import ru.itech.sbertrack.platform.agent.domain.port.AgentGatewayPort
import ru.itech.sbertrack.platform.agent.infrastructure.adapter.AgentMockAdapter

@Component
@org.springframework.context.annotation.Primary
class MistralAgentGatewayAdapter(
    private val properties: MistralProperties,
    private val restTemplate: RestTemplate,
    private val mockAdapter: AgentMockAdapter,
    private val agentDataPort: AgentDataPort,
) : AgentGatewayPort {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun generateAssistantResponse(
        agentDefinition: AgentDefinition,
        history: List<AgentMessage>,
        userMessage: String,
    ): String {
        val startTime = System.currentTimeMillis()
        return try {
            val request = buildRequest(agentDefinition, history, userMessage)
            val response = callMistralApi(request)
            val latency = System.currentTimeMillis() - startTime
            if (response != null) {
                logger.info("Mistral AI response received. Latency: {}ms, Agent: {}", latency, agentDefinition.code)
                response
            } else {
                logger.warn("Mistral AI returned empty response. Latency: {}ms, Agent: {}. Falling back to mock.", latency, agentDefinition.code)
                fallbackToMock(agentDefinition, history, userMessage, "Empty response from API")
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            logger.error(
                "Mistral AI API error after {}ms: {}. Falling back to mock. Agent: {}",
                latency,
                e.message,
                agentDefinition.code,
                e,
            )
            fallbackToMock(agentDefinition, history, userMessage, e.message ?: "Unknown error")
        }
    }

    private fun buildRequest(
        agentDefinition: AgentDefinition,
        history: List<AgentMessage>,
        userMessage: String,
    ): MistralChatRequest {
        val messages = mutableListOf<MistralMessage>()

        val masterPromptText = agentDefinition.masterPromptId
            ?.let(agentDataPort::findMasterPromptById)
            ?.promptText
            ?: "You are a helpful AI mentor."

        val systemContent = """
            $masterPromptText

            Your current specialization is: ${agentDefinition.specialization}.

            CRITICAL INSTRUCTIONS:
            - Return plain text only.
            - Do NOT use Markdown formatting.
            - Do NOT use headings, tables, links, or code blocks.
            - If a list is necessary, use only simple numbered or dash lists.
            - Do NOT provide the final solution or complete code.
            - Guide the student by asking challenging, open-ended questions.
            - Help them structure their thinking and identify gaps in their logic.
            - Be concise, supportive, and professional.
            - If the user asks for the answer, politely refuse and guide them back to the problem.
        """.trimIndent()

        messages.add(MistralMessage(role = "system", content = systemContent))

        history.forEach { msg ->
            val role = when (msg.role) {
                AgentMessageRole.USER -> "user"
                AgentMessageRole.AGENT -> "assistant"
            }
            messages.add(MistralMessage(role = role, content = msg.content))
        }

        messages.add(MistralMessage(role = "user", content = userMessage))

        return MistralChatRequest(
            model = properties.model,
            messages = messages,
        )
    }

    private fun callMistralApi(request: MistralChatRequest): String? {
        if (properties.apiKey.isBlank()) {
            logger.warn("Mistral API key is blank. Falling back to mock.")
            return null
        }

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(properties.apiKey)
        }

        val entity = HttpEntity(request, headers)
        return try {
            val response = restTemplate.postForEntity(
                "${properties.baseUrl}/chat/completions",
                entity,
                MistralChatResponse::class.java,
            )
            response.body?.choices?.firstOrNull()?.message?.content
        } catch (e: HttpStatusCodeException) {
            logger.warn("Mistral API HTTP error: {} - {}", e.statusCode, e.getMostSpecificCause().message)
            null
        }
    }

    private fun fallbackToMock(
        agentDefinition: AgentDefinition,
        history: List<AgentMessage>,
        userMessage: String,
        reason: String,
    ): String {
        logger.info("Falling back to AgentMockAdapter. Reason: {}. Agent: {}", reason, agentDefinition.code)
        return mockAdapter.generateAssistantResponse(agentDefinition, history, userMessage)
    }
}
