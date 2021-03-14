package com.bme.jnsbbk.oauthserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OAuthAuthorizationServerApplication

fun main(args: Array<String>) {
	runApplication<OAuthAuthorizationServerApplication>(*args)
}
