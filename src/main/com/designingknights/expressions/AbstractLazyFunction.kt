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

import java.util.Locale

/**
 * Abstract implementation of a lazy function which implements all necessary
 * methods with the exception of the main logic.
 */
abstract class AbstractLazyFunction
/**
 * Creates a new function with given name and parameter count.
 *
 * @param name            The name of the function.
 * @param numParams       The number of parameters for this function.
 * `-1` denotes a variable number of parameters.
 * @param booleanFunction Whether this function is a boolean function.
 */
protected constructor(name: String, numParams: Int, booleanFunction: Boolean = false) : LazyFunction {
    /**
     * Name of this function.
     */
    final override var name: String
        protected set
    /**
     * Number of parameters expected for this function. `-1`
     * denotes a variable number of parameters.
     */
    final override var numParams: Int = 0
        protected set

    /**
     * Whether this function is a boolean function.
     */
    final override var isBooleanFunction: Boolean = false
        protected set

    init {
        this.name = name.toUpperCase(Locale.ROOT)
        this.numParams = numParams
        this.isBooleanFunction = booleanFunction
    }

    override fun numParamsVaries(): Boolean {
        return numParams < 0
    }
}

