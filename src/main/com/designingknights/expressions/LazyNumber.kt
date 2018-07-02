package com.designingknights.expressions

import java.math.BigDecimal

/**
 * LazyNumber interface created for lazily evaluated functions
 */
interface LazyNumber {

    val string: String?
    fun eval(): BigDecimal?
}
