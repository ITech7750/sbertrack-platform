package ru.itech.sbertrack.platform.moderation.application.service

import org.springframework.stereotype.Service
import ru.itech.sbertrack.platform.agent.application.service.AgentService
import ru.itech.sbertrack.platform.agent.dto.response.AgentDefinitionResponse
import ru.itech.sbertrack.platform.challengecase.application.service.CaseService
import ru.itech.sbertrack.platform.challengecase.domain.model.CaseStatus
import ru.itech.sbertrack.platform.challengecase.dto.response.CaseResponse
import ru.itech.sbertrack.platform.feedback.application.service.FeedbackService
import ru.itech.sbertrack.platform.feedback.dto.response.FeedbackResponse
import java.util.UUID

@Service
class ModerationService(
    private val caseService: CaseService,
    private val feedbackService: FeedbackService,
    private val agentService: AgentService,
) {
    fun casesOnModeration(): List<CaseResponse> =
        caseService.list(trackId = null, status = CaseStatus.MODERATION, difficulty = null, tag = null, competency = null)

    fun approveCase(id: UUID): CaseResponse =
        caseService.publish(id)

    fun rejectCase(id: UUID): CaseResponse =
        caseService.rejectModeration(id)

    fun feedbackQuality(): List<FeedbackResponse> =
        feedbackService.listAll()

    fun agents(): List<AgentDefinitionResponse> =
        agentService.listAgents()
}
