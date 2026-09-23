package ru.itech.sbertrack.platform.agent.domain.model

/** Provider-neutral text request. The latest user turn occurs exactly once. */
data class AgentGenerationRequest(
    val systemPrompt: String,
    val messages: List<GenerationMessage>,
)
data class GenerationMessage(val role: AgentMessageRole, val content: String)
