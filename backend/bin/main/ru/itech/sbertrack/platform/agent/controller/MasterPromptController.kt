package ru.itech.sbertrack.platform.agent.controller

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itech.sbertrack.platform.agent.application.service.MasterPromptService
import ru.itech.sbertrack.platform.agent.dto.request.MasterPromptCreateRequest
import ru.itech.sbertrack.platform.agent.dto.request.MasterPromptUpdateRequest
import ru.itech.sbertrack.platform.agent.dto.response.MasterPromptResponse
import java.util.UUID

@Tag(name = "Master prompts")
@RestController
@RequestMapping("/api/v1/master-prompts")
class MasterPromptController(
    private val masterPromptService: MasterPromptService,
) {
    @GetMapping
    fun list(): List<MasterPromptResponse> =
        masterPromptService.list()

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): MasterPromptResponse =
        masterPromptService.get(id)

    @PostMapping
    fun create(
        @Valid @RequestBody request: MasterPromptCreateRequest,
        @RequestHeader("Authorization", required = false) authorization: String?,
    ): MasterPromptResponse =
        masterPromptService.create(request, authorization)

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody request: MasterPromptUpdateRequest): MasterPromptResponse =
        masterPromptService.update(id, request)

    @PostMapping("/{id}/activate")
    fun activate(@PathVariable id: UUID): MasterPromptResponse =
        masterPromptService.activate(id)

    @PostMapping("/{id}/archive")
    fun archive(@PathVariable id: UUID): MasterPromptResponse =
        masterPromptService.archive(id)
}
