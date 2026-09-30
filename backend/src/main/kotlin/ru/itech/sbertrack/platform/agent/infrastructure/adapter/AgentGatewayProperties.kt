package ru.itech.sbertrack.platform.agent.infrastructure.adapter

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.net.URI

@Component
@ConfigurationProperties("sbertrack.agent")
class AgentGatewayProperties {
    var activeProfile: String = "mock"
    var fallbackProfile: String = ""
    var maxInputChars: Int = 32000
    var maxHistoryMessages: Int = 20
    var profiles: Map<String, ModelProfile> = mapOf("mock" to ModelProfile())

    fun validate() {
        require(maxInputChars in 2000..200000 && maxHistoryMessages in 1..100)
        require(activeProfile in profiles) { "Unknown active agent profile" }
        require(fallbackProfile.isBlank() || fallbackProfile in profiles) { "Unknown fallback agent profile" }
        require(fallbackProfile != activeProfile) { "Fallback must differ from active profile" }
        listOf(activeProfile, fallbackProfile).filter(String::isNotBlank).forEach { name ->
            require(name.matches(Regex("[a-zA-Z0-9_-]{1,64}"))) { "Invalid agent profile name" }
            profiles.getValue(name).validate()
        }
    }
}

enum class ModelProtocol { MOCK, ANTHROPIC, GEMINI, OPENAI_COMPATIBLE }

class ModelProfile {
    var protocol: ModelProtocol = ModelProtocol.MOCK
    var baseUrl: String = ""
    var model: String = ""
    var apiKey: String = ""
    var connectTimeoutMs: Int = 5000
    var readTimeoutMs: Int = 30000
    var maxOutputTokens: Int = 1024
    var maxAttempts: Int = 2
    var retryDelayMs: Long = 500
    // Some compatible endpoints use max_completion_tokens instead of max_tokens.
    var tokenLimitField: String = "max_tokens"

    fun validate() {
        require(connectTimeoutMs in 100..60000 && readTimeoutMs in 100..120000)
        require(maxOutputTokens in 1..32768 && maxAttempts in 1..3 && retryDelayMs in 0..5000)
        require(tokenLimitField in setOf("max_tokens", "max_completion_tokens"))
        if (protocol == ModelProtocol.MOCK) return
        val uri = URI(baseUrl)
        require(uri.scheme in setOf("http", "https") && uri.host != null && uri.userInfo == null &&
            uri.query == null && uri.fragment == null) { "Agent base URL must be an HTTP(S) endpoint without credentials or query" }
        require(model.isNotBlank() && model.length <= 200 && !model.contains('\n')) { "Agent model is required" }
        if (protocol in setOf(ModelProtocol.ANTHROPIC, ModelProtocol.GEMINI)) {
            require(apiKey.isNotBlank()) { "Selected agent profile requires an API key" }
        }
    }
}
