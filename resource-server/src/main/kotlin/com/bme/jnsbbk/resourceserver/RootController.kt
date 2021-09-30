package com.bme.jnsbbk.resourceserver

import com.bme.jnsbbk.resourceserver.config.AppConfig
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class RootController(
    val appConfig: AppConfig
) {

    @GetMapping
    fun handleRequest() {
        /*val client = WebClient.builder()
            .baseUrl(appConfig.authorizationServer.url)
            .defaultHeader("Authorization")
            .build()
        client
            .get()
            .uri(appConfig.authorizationServer.authenticationEndpoint)*/
    }
}