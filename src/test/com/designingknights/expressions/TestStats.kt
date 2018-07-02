package com.designingknights.expressions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

import java.math.BigDecimal


class TestStats {
    @Test
    @Throws(Exception::class)
    fun testSum() {
        val bigDecimals = ArrayList<BigDecimal>()

        bigDecimals.add(BigDecimal("1"))
        bigDecimals.add(BigDecimal("2"))
        bigDecimals.add(BigDecimal("3"))
        bigDecimals.add(BigDecimal("4"))

        val expression = Expression("SUM(X)")
        expression.where("X", bigDecimals)

        assertEquals("10", expression.eval()!!.toPlainString())
    }

    @Test
    @Throws(Exception::class)
    fun testSum2() {
        val bigDecimals = Array<BigDecimal>(4,{ BigDecimal("0")})
        bigDecimals[0] = BigDecimal("1")
        bigDecimals[1] = BigDecimal("2")
        bigDecimals[2] = BigDecimal("3")
        bigDecimals[3] = BigDecimal("4")
        val expression = Expression("SUM(X)")
        expression.where("X", bigDecimals)

        assertEquals("10", expression.eval()!!.toPlainString())

    }

    @Test
    @Throws(Exception::class)
    fun testAvg() {
        val bigDecimals = Array<BigDecimal>(4,{ BigDecimal("0")})
        bigDecimals[0] = BigDecimal("1")
        bigDecimals[1] = BigDecimal("2")
        bigDecimals[2] = BigDecimal("3")
        bigDecimals[3] = BigDecimal("4")
        val expression = Expression("MEAN(X)")
        expression.where("X", bigDecimals)

        assertEquals("2.5", expression.eval()!!.toPlainString())

    }

    @Test
    @Throws(Exception::class)
    fun testVariance() {
        val bigDecimals = Array<BigDecimal>(4,{ BigDecimal("0")})
        bigDecimals[0] = BigDecimal("1")
        bigDecimals[1] = BigDecimal("2")
        bigDecimals[2] = BigDecimal("3")
        bigDecimals[3] = BigDecimal("4")
        val expression = Expression("VARIANCE(X)")
        expression.where("X", bigDecimals)

        assertEquals("1.25", expression.eval()!!.toPlainString())

    }
    @Test
    @Throws(Exception::class)
    fun testStdDev() {
        val bigDecimals = Array<BigDecimal>(4,{ BigDecimal("0")})
        bigDecimals[0] = BigDecimal("1")
        bigDecimals[1] = BigDecimal("2")
        bigDecimals[2] = BigDecimal("3")
        bigDecimals[3] = BigDecimal("4")
        val expression = Expression("STDDEV(X)")
        expression.where("X", bigDecimals)

        assertEquals("1.118034", expression.eval()!!.toPlainString())

    }


}