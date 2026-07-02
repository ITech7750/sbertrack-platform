package ru.itech.sbertrack.platform.common.exception

import org.springframework.http.HttpStatus

class BadRequestException(message: String) : ApiException(HttpStatus.BAD_REQUEST, message)
