package com.bme.jnsbbk.resourceserver

data class Parameters(
    val id: String,
    val secret: String,
    val scope: Set<String>
)
