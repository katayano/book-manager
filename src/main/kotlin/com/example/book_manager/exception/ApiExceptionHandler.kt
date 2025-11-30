package com.example.book_manager.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<ApiErrorResponse> {
        val body = ApiErrorResponse(message = ex.message ?: "Not found")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body)
    }

    @ExceptionHandler(BusinessRuleViolationException::class)
    fun handleBusinessRuleViolation(ex: BusinessRuleViolationException): ResponseEntity<ApiErrorResponse> {
        val body = ApiErrorResponse(message = ex.message ?: "Business rule violation")
        return ResponseEntity.badRequest().body(body)
    }

    private fun Exception.bindingErrors(): List<String> = when (this) {
        is MethodArgumentNotValidException -> bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        is BindException -> bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        else -> emptyList()
    }
}

data class ApiErrorResponse(
    val message: String,
    val details: List<String> = emptyList()
)

