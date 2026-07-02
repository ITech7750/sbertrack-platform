package ru.itech.sbertrack.platform.admin.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import ru.itech.sbertrack.platform.agent.dto.response.AgentDefinitionResponse
import ru.itech.sbertrack.platform.challengecase.dto.response.CaseResponse
import ru.itech.sbertrack.platform.submission.dto.response.SubmissionResponse
import ru.itech.sbertrack.platform.track.dto.response.TrackResponse
import ru.itech.sbertrack.platform.user.dto.response.UserResponse

@Schema(description = "Данные системной панели администратора")
data class AdminDashboardResponse(
    val statistics: AdminStatisticsResponse,
    val users: List<UserResponse>,
    val tracks: List<TrackResponse>,
    val cases: List<CaseResponse>,
    val submissions: List<SubmissionResponse>,
    val agents: List<AgentDefinitionResponse>,
)
