package com.example.ability.bot.util

import com.example.ability.bot.util.KeyboardFactory.Companion.replyKeyboard
import org.telegram.abilitybots.api.sender.SilentSender
import org.telegram.telegrambots.meta.api.methods.ActionType
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove

class ResponseHandler(private val sender: SilentSender) {

    fun sendMessage(message: String, chatId: Long, typingAction: Boolean = true) {
        if (typingAction)
            executeTypingAction(chatId)

        sender.send(message, chatId)
    }

    fun sendMessageAndForceReply(message: String, chatId: Long, typingAction: Boolean = true) {
        if (typingAction)
            executeTypingAction(chatId)

        sender.forceReply(message, chatId)
    }

    fun sendMessageAddReplyKeyboard(message: String, chatId: Long, typingAction: Boolean = true, keyboardOptions: Array<String>) {
        sendMessageWithReplyMarkup(message, chatId, typingAction, replyKeyboard(*keyboardOptions))
    }

    fun sendMessageRemoveReplyKeyboard(message: String, chatId: Long, typingAction: Boolean = true) {
        sendMessageWithReplyMarkup(message, chatId, typingAction, ReplyKeyboardRemove(true))
    }

    private fun sendMessageWithReplyMarkup(message: String, chatId: Long, typingAction: Boolean = true, replyMarkup: ReplyKeyboard) {
        if (typingAction)
            executeTypingAction(chatId)

        val sendMessage = SendMessage.builder()
            .chatId(chatId)
            .text(message)
            .replyMarkup(replyMarkup)
            .build()

        sender.execute(sendMessage)
    }

    fun executeTypingAction(chatId: Long) {
        val sendChatAction = SendChatAction()
        sendChatAction.setChatId(chatId)
        sendChatAction.setAction(ActionType.TYPING)
        sender.execute(sendChatAction)
    }

}