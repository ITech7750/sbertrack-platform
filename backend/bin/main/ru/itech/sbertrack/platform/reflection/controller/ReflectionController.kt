package ru.itech.sbertrack.platform.reflection.controller

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itech.sbertrack.platform.reflection.application.service.ReflectionService
import ru.itech.sbertrack.platform.reflection.dto.request.ReflectionCreateRequest
import ru.itech.sbertrack.platform.reflection.dto.response.ReflectionResponse
import java.util.UUID

@Tag(name = "Reflection")
@RestController
@RequestMapping("/api/v1/reflections")
class ReflectionController(
    private val reflectionService: ReflectionService,
) {
    @GetMapping("/{submissionId}")
    fun get(@PathVariable submissionId: UUID): ReflectionResponse =
        reflectionService.getBySubmission(submissionId)

    @PostMapping
    fun create(
        @Valid @RequestBody request: ReflectionCreateRequest,
        @RequestHeader("Authorization", required = false) authorization: String?,
    ): ReflectionResponse =
        reflectionService.create(request, authorization)
}
