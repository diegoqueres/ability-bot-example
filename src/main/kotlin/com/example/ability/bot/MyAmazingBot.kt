package com.example.ability.bot

import com.example.ability.bot.ability.BotAbilitySave
import com.example.ability.bot.ability.BotAbilityStart
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.telegrambots.meta.api.methods.ActionType
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction

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