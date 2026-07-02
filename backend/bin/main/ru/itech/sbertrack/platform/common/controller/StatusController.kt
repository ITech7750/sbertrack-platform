package ru.itech.sbertrack.platform.common.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itech.sbertrack.platform.common.dto.StatusResponse

@Tag(name = "Status")
@RestController
@RequestMapping("/api/v1")
class StatusController {
    @GetMapping("/status")
    fun status(): StatusResponse =
        StatusResponse(status = "OK", message = "СберТрек Платформа backend is running")
}
