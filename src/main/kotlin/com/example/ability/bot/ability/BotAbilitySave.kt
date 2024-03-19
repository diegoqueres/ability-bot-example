package com.example.ability.bot.ability

import com.example.ability.bot.enums.UserState.Save
import com.example.ability.bot.model.Product
import com.example.ability.bot.util.DbContextHandler
import com.example.ability.bot.util.PredicateUtils.Companion.hasMessageWith
import com.example.ability.bot.util.PredicateUtils.Companion.isInExpectedState
import com.example.ability.bot.util.PredicateUtils.Companion.isMessageNotEmpty
import com.example.ability.bot.util.PredicateUtils.Companion.isReplyToBot
import com.example.ability.bot.util.PredicateUtils.Companion.isReplyToMessage
import com.example.ability.bot.util.ResponseHandler
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.bot.BaseAbilityBot
import org.telegram.abilitybots.api.objects.*
import org.telegram.abilitybots.api.util.AbilityExtension
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.objects.Update
import java.math.BigDecimal

class BotAbilitySave(
    private val bot: AbilityBot,
    private val dbContextHandler: DbContextHandler = DbContextHandler(bot.db()),
    private val responseHandler: ResponseHandler = ResponseHandler(bot.silent())
) : AbilityExtension {
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

    fun start(): ReplyFlow {
        return ReplyFlow.builder(bot.db())
            .onlyIf(Flag.TEXT)
            .onlyIf(hasMessageWith("/$command"))
            .action { _: BaseAbilityBot, upd: Update ->
                dbContextHandler.initMaps(Save.MAP_USER_STATE, Save.MAP_PRODUCT)
                responseHandler.sendMessage("Let's start!", AbilityUtils.getChatId(upd))
            }
            .next(askForProductName())
            .build()
    }

    private fun askForProductName(): ReplyFlow {
        return ReplyFlow.builder(bot.db())
            .onlyIf(Flag.TEXT)
            .onlyIf(isMessageNotEmpty())
            .action { _: BaseAbilityBot, upd: Update ->
                val chatId = AbilityUtils.getChatId(upd)
                dbContextHandler.putToMap(Save.MAP_USER_STATE, chatId, Save.ASK_PRODUCT_NAME)
                responseHandler.sendMessageAndForceReply("Enter Product Name", chatId)
            }
            .next(askForProductPrice())
            .build()
    }

    private fun askForProductPrice(): ReplyFlow {
        return ReplyFlow.builder(bot.db())
            .onlyIf(Flag.TEXT)
            .onlyIf(Flag.REPLY).onlyIf(isReplyToBot(bot))
            .onlyIf(isReplyToMessage("Enter Product Name"))
            .onlyIf(isInExpectedState(dbContextHandler, Save.MAP_USER_STATE, Save.ASK_PRODUCT_NAME))
            .action { _: BaseAbilityBot, upd: Update ->
                try {
                    val chatId = AbilityUtils.getChatId(upd)
                    dbContextHandler.putToMap(Save.MAP_USER_STATE, chatId, Save.ASK_PRODUCT_PRICE)

                    val product = Product(name = upd.message.text)
                    dbContextHandler.putToMap(Save.MAP_PRODUCT, chatId, product)

                    responseHandler.sendMessageAndForceReply("Enter Product Price", chatId)
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
            .onlyIf(Flag.REPLY).onlyIf(isReplyToBot(bot))
            .onlyIf(isReplyToMessage("Enter Product Price"))
            .onlyIf(isInExpectedState(dbContextHandler, Save.MAP_USER_STATE, Save.ASK_PRODUCT_PRICE))
            .action { _: BaseAbilityBot, upd: Update ->
                try {
                    val chatId = AbilityUtils.getChatId(upd)
                    dbContextHandler.putToMap(Save.MAP_USER_STATE, chatId, Save.ASK_CONFIRMATION)

                    val product = dbContextHandler.getFromMap(Save.MAP_PRODUCT, chatId) as? Product
                        ?: throw Exception("Fail to recover product data")
                    product.price = BigDecimal(upd.message.text)
                    dbContextHandler.putToMap(Save.MAP_PRODUCT, chatId, product)

                    val productDescription = """The new product must be:
                        |name: ${product.name}
                        |price: ${product.price}"""
                    responseHandler.sendMessage(productDescription.trimMargin(), chatId)

                    responseHandler.sendMessageAddReplyKeyboard(
                        "Do you confirm save?", chatId,
                        keyboardOptions = arrayOf("Confirm", "Cancel")
                    )
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
                    val chatId = AbilityUtils.getChatId(upd)
                    val product = dbContextHandler.getFromMap(Save.MAP_PRODUCT, chatId) as? Product
                        ?: throw Exception("Fail to recover product data")

                    // Save product on your database
                    // val savedProduct = productService.save(product)
                    // --

                    val message = "Product '${product.name}' was saved successfully!"
                    responseHandler.sendMessageRemoveReplyKeyboard(message, chatId)
                } catch (e: Exception) {
                    e.printStackTrace()
                    responseHandler.sendMessageRemoveReplyKeyboard("Fail to save product", AbilityUtils.getChatId(upd))
                } finally {
                    dbContextHandler.removeFromMap(Save.MAP_USER_STATE, AbilityUtils.getChatId(upd))
                    dbContextHandler.removeFromMap(Save.MAP_PRODUCT, AbilityUtils.getChatId(upd))
                }
            },
            Flag.TEXT,
            hasMessageWith("Confirm"),
            isInExpectedState(dbContextHandler, Save.MAP_USER_STATE, Save.ASK_CONFIRMATION)
        )
    }

    private fun cancel(): Reply {
        return Reply.of(
            { _: BaseAbilityBot, upd: Update ->
                try {
                    val chatId = AbilityUtils.getChatId(upd)
                    dbContextHandler.removeFromMap(Save.MAP_USER_STATE, chatId)
                    dbContextHandler.removeFromMap(Save.MAP_PRODUCT, chatId)

                    responseHandler.sendMessageRemoveReplyKeyboard("Canceled successfully", chatId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            Flag.TEXT,
            hasMessageWith("Cancel"),
            isInExpectedState(dbContextHandler, Save.MAP_USER_STATE, Save.ASK_CONFIRMATION)
        )
    }

}