package ru.itech.sbertrack.platform.agent.infrastructure.adapter.mistral

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "sbertrack.mistral")
data class MistralProperties(
    val apiKey: String,
    val baseUrl: String,
    val model: String
)
