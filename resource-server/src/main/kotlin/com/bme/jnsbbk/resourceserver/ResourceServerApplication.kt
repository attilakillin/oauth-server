package com.bme.jnsbbk.resourceserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class ResourceServerApplication

fun main(args: Array<String>) {
    runApplication<ResourceServerApplication>(*args)
}
