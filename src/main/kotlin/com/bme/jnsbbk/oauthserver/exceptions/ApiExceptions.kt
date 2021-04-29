package com.bme.jnsbbk.oauthserver.exceptions

import org.springframework.http.HttpStatus

/**
 * Base API exception class. When instances of this class are thrown, a custom
 * [ApiExceptionHandler] intercepts the calls, and sends JSON error messages to the caller.
 */
open class ApiException (val status: HttpStatus, message: String? = null) : Exception(message)

/** A specific [ApiException] with a HTTP 400 status code. */
class BadRequestException(message: String? = null) : ApiException(HttpStatus.BAD_REQUEST, message)

/** Throws a [BadRequestException] with the specified error [message]. */
fun badRequest(message: String? = null): Nothing = throw BadRequestException(message)

/** A specific [ApiException] with a HTTP 401 status code. */
class UnauthorizedException(message: String? = null) : ApiException(HttpStatus.UNAUTHORIZED, message)

/** Throws an [UnauthorizedException] with the specified error [message]. */
fun unauthorized(message: String? = null): Nothing = throw UnauthorizedException(message)