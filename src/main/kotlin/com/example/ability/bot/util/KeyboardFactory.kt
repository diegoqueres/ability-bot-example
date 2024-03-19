package com.example.ability.bot.util

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

class KeyboardFactory {
    companion object {

        fun replyKeyboard(vararg options: String): ReplyKeyboard {
            val row = KeyboardRow()
            options.forEach(row::add)

            return ReplyKeyboardMarkup.builder()
                .keyboardRow(row)
                .oneTimeKeyboard(true)
                .resizeKeyboard(true)
                .build()
        }

    }
}