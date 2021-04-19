package com.bme.jnsbbk.oauthserver.utils

import java.util.*

fun <T> Optional<T>.getOrNull(): T? = if (isPresent) get() else null