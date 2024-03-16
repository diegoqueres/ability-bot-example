package com.example.ability.bot

import java.io.Serializable
import java.math.BigDecimal

data class Product(
    var name: String? = null,
    var price: BigDecimal? = null
) : Serializable