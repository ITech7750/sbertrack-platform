package ru.itech.sbertrack.platform.moderation.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itech.sbertrack.platform.agent.dto.response.AgentDefinitionResponse
import ru.itech.sbertrack.platform.challengecase.dto.response.CaseResponse
import ru.itech.sbertrack.platform.feedback.dto.response.FeedbackResponse
import ru.itech.sbertrack.platform.moderation.application.service.ModerationService
import java.util.UUID

@Tag(name = "Moderation")
@RestController
@RequestMapping("/api/v1/moderation")
class ModerationController(
    private val moderationService: ModerationService,
) {
    @GetMapping("/cases")
    fun cases(): List<CaseResponse> =
        moderationService.casesOnModeration()

    @PostMapping("/cases/{id}/approve")
    fun approveCase(@PathVariable id: UUID): CaseResponse =
        moderationService.approveCase(id)

    @PostMapping("/cases/{id}/reject")
    fun rejectCase(@PathVariable id: UUID): CaseResponse =
        moderationService.rejectCase(id)

    @GetMapping("/feedback")
    fun feedback(): List<FeedbackResponse> =
        moderationService.feedbackQuality()

    @GetMapping("/agents")
    fun agents(): List<AgentDefinitionResponse> =
        moderationService.agents()
}
