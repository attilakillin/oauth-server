package com.bme.jnsbbk.oauthserver.exceptions

import org.springframework.http.HttpStatus

open class ApiException (val status: HttpStatus, message: String? = null) : Exception(message)

class BadRequestException(message: String? = null) : ApiException(HttpStatus.BAD_REQUEST, message)
class UnauthorizedException(message: String? = null) : ApiException(HttpStatus.UNAUTHORIZED, message)