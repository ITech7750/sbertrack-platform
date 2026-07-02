package ru.itech.sbertrack.platform.common.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.itech.sbertrack.platform.common.dto.ErrorResponse

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ApiException::class)
    fun handleApiException(exception: ApiException, request: HttpServletRequest): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(exception.httpStatus).body(
            ErrorResponse(
                status = exception.httpStatus.value(),
                error = exception.httpStatus.reasonPhrase,
                message = exception.message,
                path = request.requestURI,
            ),
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(exception: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val message = exception.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return badRequest(message.ifBlank { "Некорректный запрос" }, request)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraint(exception: ConstraintViolationException, request: HttpServletRequest): ResponseEntity<ErrorResponse> =
        badRequest(exception.message ?: "Некорректный запрос", request)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(exception: IllegalArgumentException, request: HttpServletRequest): ResponseEntity<ErrorResponse> =
        badRequest(exception.message ?: "Некорректный запрос", request)

    private fun badRequest(message: String, request: HttpServletRequest): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.reasonPhrase,
                message = message,
                path = request.requestURI,
            ),
        )
}
