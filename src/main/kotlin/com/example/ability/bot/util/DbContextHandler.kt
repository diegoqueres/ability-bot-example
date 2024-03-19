package com.example.ability.bot.util

import org.telegram.abilitybots.api.db.DBContext


class DbContextHandler(private val db: DBContext) {

    fun initMaps(vararg identifier: String) {
        identifier.forEach { getMap(it) }
    }

    fun getMap(identifier: String): MutableMap<Long, Any> = db.getMap(identifier)

    fun putToMap(identifier: String, chatId: Long, value: Any) {
        this.getMap(identifier)[chatId] = value
    }

    fun getFromMap(identifier: String, chatId: Long): Any? = this.getMap(identifier)[chatId]

    fun removeFromMap(identifier: String, chatId: Long) = this.getMap(identifier).remove(chatId)

}