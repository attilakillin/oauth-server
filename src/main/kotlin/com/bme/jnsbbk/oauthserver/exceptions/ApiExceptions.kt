package com.bme.jnsbbk.oauthserver.exceptions

import org.springframework.http.HttpStatus

open class ApiException (val status: HttpStatus, message: String? = null): Exception(message)