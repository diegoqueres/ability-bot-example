package com.example.ability.bot.util

import org.telegram.abilitybots.api.bot.BaseAbilityBot
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.function.Predicate

class PredicateUtils {
    companion object {

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

        fun isReplyToBot(bot: BaseAbilityBot): Predicate<Update> {
            return Predicate<Update> { upd: Update ->
                upd.message.replyToMessage.from.userName.equals(bot.botUsername, ignoreCase = true)
            }
        }

        fun isInExpectedState(dbContextHandler: DbContextHandler, idUserStateMap: String, expectedState: Any): Predicate<Update> {
            return Predicate<Update> { upd: Update ->
                dbContextHandler.getFromMap(idUserStateMap, AbilityUtils.getChatId(upd)) == expectedState
            }
        }

    }
}