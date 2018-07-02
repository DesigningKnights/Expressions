
package com.designingknights.expressions


import java.math.BigDecimal

/**
 * Abstract implementation of an operator.
 */
abstract class AbstractOperator : AbstractLazyOperator, Operator {
    /**
     * Creates a new operator.
     *
     * @param oper            The operator name (pattern).
     * @param precedence      The operators precedence.
     * @param leftAssoc       `true` if the operator is left associative,
     * else `false`.
     * @param booleanOperator Whether this operator is boolean.
     */
    protected constructor(oper: String, precedence: Int, leftAssoc: Boolean, booleanOperator: Boolean) : super(oper, precedence, leftAssoc, booleanOperator) {}

    /**
     * Creates a new operator.
     *
     * @param oper       The operator name (pattern).
     * @param precedence The operators precedence.
     * @param leftAssoc  `true` if the operator is left associative,
     * else `false`.
     */
    protected constructor(oper: String, precedence: Int, leftAssoc: Boolean) : super(oper, precedence, leftAssoc)

    override fun eval(v1: LazyNumber, v2: LazyNumber?): LazyNumber {
        return object : LazyNumber {

            override val string: String
                get() = this@AbstractOperator.eval(v1.eval(), v2!!.eval()).toString()

            override fun eval(): BigDecimal {
                return this@AbstractOperator.eval(v1.eval(), v2!!.eval())
            }
        }
    }
}
