package ru.itech.sbertrack.platform.cvbook.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.itech.sbertrack.platform.common.model.Competency
import ru.itech.sbertrack.platform.cvbook.application.service.CvBookService
import ru.itech.sbertrack.platform.cvbook.dto.response.CvBookCandidateResponse
import java.util.UUID

@Tag(name = "CV-book")
@RestController
@RequestMapping("/api/v1/cv-book/candidates")
class CvBookController(
    private val cvBookService: CvBookService,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) competency: Competency?,
        @RequestParam(required = false) minScore: Int?,
        @RequestParam(required = false) caseTag: String?,
        @RequestParam(required = false) organizationName: String?,
        @RequestParam(required = false) priorityOnly: Boolean?,
        @RequestParam(required = false) completedCasesMin: Int?,
    ): List<CvBookCandidateResponse> =
        cvBookService.list(competency, minScore, caseTag, organizationName, priorityOnly, completedCasesMin)

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): CvBookCandidateResponse =
        cvBookService.get(id)
}
