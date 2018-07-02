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
 * Base interface which is required for lazily evaluated functions. A function
 * is defined by a name, a number of parameters it accepts and of course
 * the logic for evaluating the result.
 */
interface LazyFunction {

    /**
     * Gets the name of this function.<br></br>
     * <br></br>
     * The name is use to invoke this function in the expression.
     *
     * @return The name of this function.
     */
    val name: String

    /**
     * Gets the number of parameters this function accepts.<br></br>
     * <br></br>
     * A value of `-1` denotes that this function accepts a variable
     * number of parameters.
     *
     * @return The number of parameters this function accepts.
     */
    val numParams: Int

    /**
     * Gets whether this function evaluates to a boolean expression.
     *
     * @return `true` if this function evaluates to a boolean
     * expression.
     */
    val isBooleanFunction: Boolean

    /**
     * Gets whether the number of accepted parameters varies.<br></br>
     * <br></br>
     * That means that the function does accept an undefined amount of
     * parameters.
     *
     * @return `true` if the number of accepted parameters varies.
     */
    fun numParamsVaries(): Boolean

    /**
     * Lazily evaluate this function.
     *
     * @param lazyParams The accepted parameters.
     * @return The lazy result of this function.
     */
    fun lazyEval(lazyParams: List<LazyNumber>): LazyNumber
}
