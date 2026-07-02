package ru.itech.sbertrack.platform.common.exception

import org.springframework.http.HttpStatus

class UnauthorizedException(message: String = "Требуется вход в систему") : ApiException(HttpStatus.UNAUTHORIZED, message)
