/*
 * Copyright 2018 Timothy Winters
 *
 * https://timothywinters.pro
 * https://about.me/TimothyRWinters
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.designingknights.expressions

import java.math.BigDecimal

/**
 * Abstract implementation of an unary operator.<br></br>
 * <br></br>
 * This abstract implementation implements eval so that it forwards its first
 * parameter to evalUnary.
 */
abstract class AbstractUnaryOperator
/**
 * Creates a new operator.
 *
 * @param oper            The operator name (pattern).
 * @param precedence      The operators precedence.
 * @param leftAssoc       `true` if the operator is left associative,
 * else `false`.
 */
protected constructor(oper: String, precedence: Int, leftAssoc: Boolean) : AbstractOperator(oper, precedence, leftAssoc) {

    override fun eval(v1: LazyNumber, v2: LazyNumber?): LazyNumber {
        if (v2 != null) {
            throw ExpressionException("Did not expect a second parameter for unary operator")
        }
        return object : LazyNumber {
            override val string: String
                get() = this@AbstractUnaryOperator.evalUnary(v1.eval()).toString()

            override fun eval(): BigDecimal {
                return this@AbstractUnaryOperator.evalUnary(v1.eval())
            }
        }
    }

    override fun eval(v1: BigDecimal?, v2: BigDecimal?): BigDecimal {
        if (v2 != null) {
            throw ExpressionException("Did not expect a second parameter for unary operator")
        }
        return evalUnary(v1)
    }

    /**
     * Implementation of this unary operator.
     *
     * @param v1 The parameter.
     * @return The result of the operation.
     */
    abstract fun evalUnary(v1: BigDecimal?): BigDecimal
}
