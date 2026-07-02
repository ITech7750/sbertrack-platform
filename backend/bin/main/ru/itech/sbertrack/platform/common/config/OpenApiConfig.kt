package ru.itech.sbertrack.platform.common.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig(
    @Value("\${sbertrack.openapi.title}") private val title: String,
    @Value("\${sbertrack.openapi.version}") private val version: String,
) {
    @Bean
    fun openApi(): OpenAPI =
        OpenAPI().info(
            Info()
                .title(title)
                .version(version)
                .description("REST API моковой платформы практических кейсов СберТрек"),
        )
}
