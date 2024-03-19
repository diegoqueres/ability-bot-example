package com.example.ability.bot

import com.example.ability.bot.ability.BotAbilitySave
import com.example.ability.bot.ability.BotAbilityStart
import com.example.ability.bot.config.TelegramBotProperties
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.bot.AbilityBot

@Component
final class MyAmazingBot(
    private val telegramBotProperties: TelegramBotProperties
) : AbilityBot(telegramBotProperties.token, telegramBotProperties.username) {

    init {
        addExtension(BotAbilityStart(this))
        addExtension(BotAbilitySave(this))
    }

    override fun creatorId(): Long {
        return telegramBotProperties.creatorId
    }

}