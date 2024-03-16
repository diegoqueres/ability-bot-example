package com.example.ability.bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class AbilityBotExampleApplication

fun main(args: Array<String>) {
	runApplication<AbilityBotExampleApplication>(*args)
}