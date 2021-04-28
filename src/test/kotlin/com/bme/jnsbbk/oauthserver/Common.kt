package com.bme.jnsbbk.oauthserver

class ValidationException : Exception()
fun onError(): Nothing = throw ValidationException()