package com.example.ability.bot.ability

import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.abilitybots.api.util.AbilityExtension

class BotAbilityStart(private val bot: AbilityBot) : AbilityExtension {
    val command = "start"
    val description = "Starts the bot"

    fun startAbility(): Ability {
        return Ability.builder()
            .name(command)
            .info(description)
            .privacy(Privacy.PUBLIC)
            .locality(Locality.ALL)
            .action { ctx -> bot.silent().send("Welcome to My Amazing Bot!", ctx.chatId()) }
            .build()
    }

}