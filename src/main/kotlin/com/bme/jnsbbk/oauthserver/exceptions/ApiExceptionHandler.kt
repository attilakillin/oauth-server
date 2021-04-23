package com.bme.jnsbbk.oauthserver.exceptions

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/** A custom exception handler. [ApiException]s thrown in any controller get processed
 *  by this handler, which creates and sends meaningful JSON error messages to the client. */
@ControllerAdvice
class ApiExceptionHandler : ResponseEntityExceptionHandler() {

    /** Handles [ApiException] instances. Returns a [ResponseEntity] with the status and
     *  error message specified in the exception. */
    @ExceptionHandler(ApiException::class)
    fun handleException(e: ApiException, request: WebRequest) =
        ResponseEntity.status(e.status).body(e.message?.let { mapOf("error" to e.message) })
}