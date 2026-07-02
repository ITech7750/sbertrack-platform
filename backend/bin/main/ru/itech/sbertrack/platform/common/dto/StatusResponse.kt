package ru.itech.sbertrack.platform.common.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Простой статус операции")
data class StatusResponse(
    val status: String,
    val message: String,
)
