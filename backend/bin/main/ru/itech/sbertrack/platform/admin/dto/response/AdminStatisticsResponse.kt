package ru.itech.sbertrack.platform.admin.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import ru.itech.sbertrack.platform.common.model.Competency

@Schema(description = "Статистика платформы")
data class AdminStatisticsResponse(
    val usersCount: Int,
    val tracksCount: Int,
    val casesCount: Int,
    val submissionsCount: Int,
    val cvBookCandidatesCount: Int,
    val activeAgentsCount: Int,
    val competencyDistribution: Map<Competency, Int>,
)
