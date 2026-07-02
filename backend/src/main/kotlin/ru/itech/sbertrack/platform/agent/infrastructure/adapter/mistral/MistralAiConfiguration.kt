package ru.itech.sbertrack.platform.agent.infrastructure.adapter.mistral

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
@EnableConfigurationProperties(MistralProperties::class)
class MistralAiConfiguration {
    @Bean
    fun restTemplate(): RestTemplate {
        val factory = SimpleClientHttpRequestFactory()
        factory.setConnectTimeout(5000)
        factory.setReadTimeout(15000)
        return RestTemplate(factory)
    }
}
