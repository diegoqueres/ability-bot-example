package com.example.ability.bot

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("telegram-bot")
data class TelegramBotProperties (
    val username: String,
    val token: String,
    val creatorId: Long
)