package com.bme.jnsbbk.oauthserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OAuthServerApplication

fun main(args: Array<String>) {
	runApplication<OAuthServerApplication>(*args)
}