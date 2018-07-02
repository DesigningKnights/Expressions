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

/**
 * Abstract implementation of an operator.
 */
abstract class AbstractLazyOperator : LazyOperator {
    /**
     * This operators name (pattern).
     */
    final override var oper: String
        protected set
    /**
     * Operators precedence.
     */
    final override var precedence: Int = 0
        protected set
    /**
     * Operator is left associative.
     */
    final override var isLeftAssoc: Boolean = false
        protected set
    /**
     * Whether this operator is boolean or not.
     */
    final override var isBooleanOperator = false
        protected set

    /**
     * Creates a new operator.
     *
     * @param oper            The operator name (pattern).
     * @param precedence      The operators precedence.
     * @param leftAssoc       `true` if the operator is left associative,
     * else `false`.
     * @param booleanOperator Whether this operator is boolean.
     */
    protected constructor(oper: String, precedence: Int, leftAssoc: Boolean, booleanOperator: Boolean) {
        this.oper = oper
        this.precedence = precedence
        this.isLeftAssoc = leftAssoc
        this.isBooleanOperator = booleanOperator
    }

    /**
     * Creates a new operator.
     *
     * @param oper       The operator name (pattern).
     * @param precedence The operators precedence.
     * @param leftAssoc  `true` if the operator is left associative,
     * else `false`.
     */
    protected constructor(oper: String, precedence: Int, leftAssoc: Boolean) {
        this.oper = oper
        this.precedence = precedence
        this.isLeftAssoc = leftAssoc
    }
}
