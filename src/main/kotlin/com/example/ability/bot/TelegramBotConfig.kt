package com.example.ability.bot

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@Configuration
class TelegramBotConfig {
    private val logger: Logger = LoggerFactory.getLogger(TelegramBotConfig::class.java)

    @EventListener
    fun onApplicationEvent(event: ContextRefreshedEvent) {
        try {
            val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
            telegramBotsApi.registerBot(event.applicationContext.getBean("myAmazingBot", AbilityBot::class.java))
            logger.info("Telegram bot has initialized successfully!")
        } catch (e: TelegramApiException) {
            logger.error("Error when initialize telegram bot: {}", e.message, e)
        }
    }

}