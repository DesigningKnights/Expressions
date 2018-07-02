package com.designingknights.expressions

/**
 * The expression evaluators exception class.
 */
class ExpressionException(message: String) : RuntimeException(message) {
    companion object {
        private val serialVersionUID = 1118142866870779047L
    }
}
