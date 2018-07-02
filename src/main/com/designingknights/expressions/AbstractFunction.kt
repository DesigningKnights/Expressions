package com.designingknights.expressions


import java.math.BigDecimal
import java.util.ArrayList

/**
 * Abstract implementation of a direct function.<br></br>
 * <br></br>
 * This abstract implementation does implement lazyEval so that it returns
 * the result of eval.
 */
abstract class AbstractFunction : AbstractLazyFunction, Function {

    /**
     * Creates a new function with given name and parameter count.
     *
     * @param name      The name of the function.
     * @param numParams The number of parameters for this function.
     * `-1` denotes a variable number of parameters.
     */
    protected constructor(name: String, numParams: Int) : super(name, numParams)

    /**
     * Creates a new function with given name and parameter count.
     *
     * @param name            The name of the function.
     * @param numParams       The number of parameters for this function.
     * `-1` denotes a variable number of parameters.
     * @param booleanFunction Whether this function is a boolean function.
     */
    protected constructor(name: String, numParams: Int, booleanFunction: Boolean) : super(name, numParams, booleanFunction)

    override fun lazyEval(lazyParams: List<LazyNumber>): LazyNumber {
        return object : LazyNumber {

            private var params: MutableList<BigDecimal>? = null

            override val string: String

                get() = this@AbstractFunction.eval(getParams()).toString()

            override fun eval(): BigDecimal {
                return this@AbstractFunction.eval(getParams())
            }

            private fun getParams(): MutableList<BigDecimal?> {
                if (params == null) {
                    params = ArrayList()
                    for (lazyParam in lazyParams) {
                        val param = lazyParam.eval()
                        if (param == null) {
                            (params as MutableList<BigDecimal?>).add(null)
                        }
                        else {
                            (params as MutableList<BigDecimal?>).add(param)
                        }
                    }
                }
                return params as MutableList<BigDecimal?>
            }
        }
    }
}
