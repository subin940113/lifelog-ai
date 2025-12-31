package com.example.lifelog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication
class LifelogAiApplication

fun main(args: Array<String>) {
    runApplication<LifelogAiApplication>(*args)
}
