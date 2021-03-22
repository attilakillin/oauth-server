package com.bme.jnsbbk.oauthserver.exceptions

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ApiExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(ApiException::class)
    fun handleException(e: ApiException, request: WebRequest): ResponseEntity<String> {
        return ResponseEntity.status(e.status).body("{\"error\": \"${e.message}\"}")
    }
}