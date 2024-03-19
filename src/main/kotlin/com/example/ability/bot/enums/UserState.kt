package com.example.ability.bot.enums

class UserState {

    enum class Save {
        ASK_PRODUCT_NAME, ASK_PRODUCT_PRICE, ASK_CONFIRMATION;
        companion object {
            const val MAP_USER_STATE = "MAP_SAVE_USER_STATE"
            const val MAP_PRODUCT = "MAP_SAVE_PRODUCT"
        }
    }

}