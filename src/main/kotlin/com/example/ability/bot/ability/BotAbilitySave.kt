package com.example.ability.bot.ability

import com.example.ability.bot.Product
import com.example.ability.bot.UserState
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.bot.BaseAbilityBot
import org.telegram.abilitybots.api.objects.*
import org.telegram.abilitybots.api.util.AbilityExtension
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.ActionType
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import java.math.BigDecimal
import java.util.function.Predicate

class BotAbilitySave(private val bot: AbilityBot) : AbilityExtension {
    companion object {
        const val SAVE_USER_STATE = "SAVE_USER_STATE"
    }
    val command = "save"
    val description = "Save a new product"

    fun saveAbility(): Ability {
        return Ability.builder()
            .name(command)
            .info(description)
            .privacy(Privacy.ADMIN)
            .locality(Locality.ALL)
            .action { _ -> }
            .build()
    }

    fun startReplyFlow(): ReplyFlow {
        return ReplyFlow.builder(bot.db())
            .onlyIf(Flag.TEXT)
            .onlyIf(hasMessageWith("/$command"))
            .action { bot: BaseAbilityBot, upd: Update ->
                bot.silent().send("Let's start!", AbilityUtils.getChatId(upd))
            }
            .next(askForProductName())
            .build()
    }

    private fun askForProductName(): ReplyFlow {
        return ReplyFlow.builder(bot.db())
            .onlyIf(Flag.TEXT)
            .onlyIf(isMessageNotEmpty())
            .action { bot: BaseAbilityBot, upd: Update ->
                initMaps()
                getUserStateMap()[AbilityUtils.getChatId(upd)] = UserState.Save.ASK_PRODUCT_NAME

                executeTypingAction(AbilityUtils.getChatId(upd))
                bot.silent().forceReply("Enter Product Name", AbilityUtils.getChatId(upd))
            }
            .next(askForProductPrice())
            .build()
    }

    private fun askForProductPrice(): ReplyFlow {
        return ReplyFlow.builder(bot.db())
            .onlyIf(Flag.TEXT)
            .onlyIf(Flag.REPLY).onlyIf(isReplyToBot())
            .onlyIf(isReplyToMessage("Enter Product Name"))
            .onlyIf{ upd -> getUserStateMap()[AbilityUtils.getChatId(upd)] == UserState.Save.ASK_PRODUCT_NAME }
            .action { bot: BaseAbilityBot, upd: Update ->
                try {
                    getUserStateMap()[AbilityUtils.getChatId(upd)] = UserState.Save.ASK_PRODUCT_PRICE

                    getProductMap()[AbilityUtils.getChatId(upd)] = Product(name = upd.message.text)

                    executeTypingAction(AbilityUtils.getChatId(upd))
                    bot.silent().forceReply("Enter Product Price", AbilityUtils.getChatId(upd))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .next(askForProductSaveConfirmation())
            .build()
    }

    private fun askForProductSaveConfirmation(): ReplyFlow {
        return ReplyFlow.builder(bot.db())
            .onlyIf(Flag.TEXT)
            .onlyIf(Flag.REPLY).onlyIf(isReplyToBot())
            .onlyIf(isReplyToMessage("Enter Product Price"))
            .onlyIf{ upd -> getUserStateMap()[AbilityUtils.getChatId(upd)] == UserState.Save.ASK_PRODUCT_PRICE }
            .action { _: BaseAbilityBot, upd: Update ->
                try {
                    getUserStateMap()[AbilityUtils.getChatId(upd)] = UserState.Save.ASK_CONFIRMATION

                    val product = getProductMap()[AbilityUtils.getChatId(upd)]
                        ?: throw Exception("Fail to recover product data")
                    product.price = BigDecimal(upd.message.text)
                    getProductMap()[AbilityUtils.getChatId(upd)] = product

                    executeTypingAction(AbilityUtils.getChatId(upd))

                    val productDescription = "The new product will be:\n" +
                            "name: ${product.name}\n" +
                            "price: ${product.price}"
                    bot.silent().send(productDescription, AbilityUtils.getChatId(upd))

                    val sendMessage = SendMessage.builder()
                        .chatId(AbilityUtils.getChatId(upd))
                        .text("Do you confirm save?")
                        .replyMarkup(replyKeyboard("Confirm", "Cancel"))
                        .build()
                    bot.silent().execute(sendMessage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .next(save())
            .next(cancel())
            .build()
    }

    private fun save(): Reply {
        return Reply.of(
            { _: BaseAbilityBot, upd: Update ->
                try {
                    val product: Product = getProductMap()[AbilityUtils.getChatId(upd)]
                        ?: throw Exception("Fail to recover product data")

                    // Save product on your database
                    // val savedProduct = productService.save(product)
                    // --

                    val sendMessage = SendMessage.builder()
                        .chatId(AbilityUtils.getChatId(upd))
                        .text("Product '${product.name}' was saved successfully!")
                        .replyMarkup(ReplyKeyboardRemove(true))     //remove reply keyboard
                        .build()
                    bot.silent().execute(sendMessage)
                } catch (e: Exception) {
                    e.printStackTrace()
                    val sendMessage = SendMessage.builder()
                        .chatId(AbilityUtils.getChatId(upd))
                        .text("Fail to save product")
                        .replyMarkup(ReplyKeyboardRemove(true))     //remove reply keyboard
                        .build()
                    bot.silent().execute(sendMessage)
                } finally {
                    getUserStateMap().remove(AbilityUtils.getChatId(upd))
                    getProductMap().remove(AbilityUtils.getChatId(upd))
                }
            },
            Flag.TEXT,
            hasMessageWith("Confirm"),
            { upd -> getUserStateMap()[AbilityUtils.getChatId(upd)] == UserState.Save.ASK_CONFIRMATION }
        )
    }

    private fun cancel(): Reply {
        return Reply.of(
            { _: BaseAbilityBot, upd: Update ->
                try {
                    getUserStateMap().remove(AbilityUtils.getChatId(upd))
                    getProductMap().remove(AbilityUtils.getChatId(upd))

                    val sendMessage = SendMessage.builder()
                        .chatId(AbilityUtils.getChatId(upd))
                        .text("Canceled successfully")
                        .replyMarkup(ReplyKeyboardRemove(true))     //remove reply keyboard
                        .build()
                    bot.silent().execute(sendMessage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            Flag.TEXT,
            hasMessageWith("Cancel"),
            { upd -> getUserStateMap()[AbilityUtils.getChatId(upd)] == UserState.Save.ASK_CONFIRMATION }
        )
    }

    private fun initMaps() {
        bot.db().getMap<Long, Product>(command)
        bot.db().getMap<Long, UserState.Save>(SAVE_USER_STATE)
    }

    private fun getProductMap(): MutableMap<Long, Product> = bot.db().getMap(command)
    private fun getUserStateMap(): MutableMap<Long, UserState.Save> = bot.db().getMap(SAVE_USER_STATE)

    fun hasMessageWith(msg: String): Predicate<Update> {
        return Predicate<Update> { upd ->
            upd.message.text.equals(msg, true)
        }
    }

    fun isMessageNotEmpty(): Predicate<Update> {
        return Predicate<Update> { upd ->
            upd.hasMessage() && upd.message.text.isNotEmpty()
        }
    }

    fun isReplyToMessage(message: String): Predicate<Update> {
        return Predicate { upd: Update ->
            val reply = upd.message.replyToMessage
            reply.hasText() && reply.text.equals(message, ignoreCase = true)
        }
    }

    fun isReplyToBot(): Predicate<Update> {
        return Predicate<Update> { upd: Update ->
            upd.message.replyToMessage.from.userName.equals(bot.botUsername, ignoreCase = true)
        }
    }

    fun executeTypingAction(chatId: Long) {
        val sendChatAction = SendChatAction()
        sendChatAction.setChatId(chatId)
        sendChatAction.setAction(ActionType.TYPING)
        bot.silent().execute(sendChatAction)
    }

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