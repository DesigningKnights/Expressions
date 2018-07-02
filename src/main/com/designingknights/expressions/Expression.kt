package com.designingknights.expressions;

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import java.util.*
import java.util.Locale
import java.math.BigDecimal.ONE

/**
 *
 */
class Expression(expression: String?, defaultMathContext: MathContext? = MathContext.DECIMAL32) {
    constructor(expression: String) : this(expression, MathContext(7, RoundingMode.HALF_UP))


    private val TWO = BigDecimal("2")
    private val THREE = BigDecimal("3")
    private val MINUS_ONE = BigDecimal("-1")

    private val DOUBLE_MAX_VALUE = BigDecimal.valueOf(java.lang.Double.MAX_VALUE)

    /**
     * Unary operators precedence: + and - as prefix
     */
    val OPERATOR_PRECEDENCE_UNARY = 60

    /**
     * Equality operators precedence: =, ==, !=. <>
     */
    val OPERATOR_PRECEDENCE_EQUALITY = 7

    /**
     * Comparative operators precedence: <,>,<=,>=
     */
    val OPERATOR_PRECEDENCE_COMPARISON = 10

    /**
     * Or operator precedence: ||
     */
    val OPERATOR_PRECEDENCE_OR = 2

    /**
     * And operator precedence: &&
     */
    val OPERATOR_PRECEDENCE_AND = 4

    /**
     * Power operator precedence: ^
     */
    val OPERATOR_PRECEDENCE_POWER = 40

    /**
     * Multiplicative operators precedence: *,/,%
     */
    val OPERATOR_PRECEDENCE_MULTIPLICATIVE = 30

    /**
     * Additive operators precedence: + and -
     */
    val OPERATOR_PRECEDENCE_ADDITIVE = 20

    /**
     * Definition of PI as a constant, can be used in expressions as variable.
     */
    val PI = BigDecimal(
            "3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679")

    /**
     * Definition of e: "Euler's number" as a constant, can be used in
     * expressions as variable.
     */
    val e = BigDecimal(
            "2.71828182845904523536028747135266249775724709369995957496696762772407663")

    /**
     * Definition of phi: "The Golden Ratio" as a constant, can be used in
     * expressions as variable.
     */
    val PHI = BigDecimal(
            "1.61803398874989484820458683436563811772030917980576286213544862270526046281890244970720720418939113")

    /**
     * Definition of the square root of 2 as a constant, can be used in
     * expressions as variable.
     */
    val sq2 = BigDecimal(
            "1.4142135623730950488016887242096980785696718753769480731766797379907324784621070388503875343276415727")

    /**
     * Definition of the square root of 3 as a constant, can be used in
     * expressions as variable.
     */
    val sq3 = BigDecimal(
            "1.7320508075688772935274463415058723669428052538103806280558069794519330169088000370811461867572485756")

    /**
     * Definition of the square root of 5 as a constant, can be used in
     * expressions as variable.
     */
    val sq5 = BigDecimal(
            "2.2360679774997896964091736687312762354406183596115257242708972454105209256378048994144144083787822749")

    /**
     * The [MathContext] to use for calculations.
     */
    private var mc: MathContext? = null

    /**
     * The characters (other than letters and digits) allowed as the first
     * character in a variable.
     */
    private var firstVarChars = "_"

    /**
     * The characters (other than letters and digits) allowed as the second or
     * subsequent characters in a variable.
     */
    private var varChars = "_"

    /**
     * The original infix expression.
     */
    private var originalExpression: String? = null

    /**
     * The current infix expression, with optional variable substitutions.
     */
    private var expression: String? = null

    /**
     * The cached RPN (Reverse Polish Notation) of the expression.
     */
    private var rpn: List<Token>? = null

    /**
     * All defined operators with name and implementation.
     */
    var operators = TreeMap<String, LazyOperator>(
            String.CASE_INSENSITIVE_ORDER)

    /**
     * All defined functions with name and implementation.
     */
    var functions = TreeMap<String, com.designingknights.expressions.LazyFunction>(
            String.CASE_INSENSITIVE_ORDER)

    /**
     * All defined variables with name and value.
     */
    var variables = TreeMap<String, LazyNumber?>(String.CASE_INSENSITIVE_ORDER)

    /**
     * What character to use for decimal separators.
     */
    private val decimalSeparator = '.'

    /**
     * What character to use for minus sign (negative values).
     */
    private val minusSign = '-'

    /**
     * The BigDecimal representation of the left parenthesis, used for parsing
     * varying numbers of function parameters.
     */
    private val PARAMS_START = object : LazyNumber {

        override val string: String?
            get() = null

        override fun eval(): BigDecimal? {
            return null
        }
    }

    /**
     * The expression evaluators exception class.
     */
    /*class ExpressionException(message: String) : RuntimeException(message) {
        companion object {
            private val serialVersionUID = 1118142866870779047L
        }
    }*/

    /**
     * LazyNumber interface created for lazily evaluated functions
     */
    /*interface LazyNumber {

        val string: String?
        fun eval(): BigDecimal?
    }*/

    interface LazyList {

        val string: String
        fun eval(): List<BigDecimal>
    }

    /**
     * Construct a LazyNumber from a BigDecimal
     */
    private fun createLazyNumber(bigDecimal: BigDecimal): LazyNumber {
        return object : LazyNumber {
            override val string: String
                get() = bigDecimal.toPlainString()

            override fun eval(): BigDecimal {
                return bigDecimal
            }
        }
    }

    private fun createLazyList(bigDecimals: List<BigDecimal>): LazyList {
        return object : LazyList {
            override val string: String
                get() = bigDecimals.toString()

            override fun eval(): List<BigDecimal> {
                return bigDecimals
            }

        }
    }

    abstract inner class LazyFunction : AbstractLazyFunction {
        /**
         * Creates a new function with given name and parameter count.
         *
         * @param name            The name of the function.
         * @param numParams       The number of parameters for this function.
         * `-1` denotes a variable number of parameters.
         * @param booleanFunction Whether this function is a boolean function.
         */
        constructor(name: String, numParams: Int, booleanFunction: Boolean) : super(name, numParams, booleanFunction)

        /**
         * Creates a new function with given name and parameter count.
         *
         * @param name      The name of the function.
         * @param numParams The number of parameters for this function.
         * `-1` denotes a variable number of parameters.
         */
        constructor(name: String, numParams: Int) : super(name, numParams) {}
    }

    /**
     * Abstract definition of a supported expression function. A function is
     * defined by a name, the number of parameters and the actual processing
     * implementation.
     */
    abstract inner class Function(name: String, numParams: Int, booleanFunction: Boolean) : AbstractFunction(name, numParams, booleanFunction) {

        constructor(name: String, numParams: Int) : this(name, numParams, false)

        //constructor(name: String, numParams: Int, booleanFunction: Boolean) : super(name, numParams, booleanFunction) {}
    }

    /**
     * Abstract definition of a supported operator. An operator is defined by
     * its name (pattern), precedence and if it is left- or right associative.
     */
    abstract inner class Operator(oper: String, precedence: Int, leftAssoc: Boolean, booleanOperator: Boolean) : AbstractOperator(oper, precedence, leftAssoc, booleanOperator) {

        /**
         * Creates a new operator.
         *
         * @param oper            The operator name (pattern).
         * @param precedence      The operators precedence.
         * @param leftAssoc       `true` if the operator is left associative,
         * else `false`.
         * @param booleanOperator Whether this operator is boolean.
         */
        //constructor(oper: String, precedence: Int, leftAssoc: Boolean, booleanOperator: Boolean) : super(oper, precedence, leftAssoc, booleanOperator) {}

        /**
         * Creates a new operator.
         *
         * @param oper       The operator name (pattern).
         * @param precedence The operators precedence.
         * @param leftAssoc  `true` if the operator is left associative,
         * else `false`.
         */
        constructor(oper: String, precedence: Int, leftAssoc: Boolean) : this(oper, precedence, leftAssoc, false)
    }

    abstract inner class UnaryOperator(oper: String, precedence: Int, leftAssoc: Boolean) : AbstractUnaryOperator(oper, precedence, leftAssoc)

    enum class TokenType {
        VARIABLE, FUNCTION, LITERAL, OPERATOR, UNARY_OPERATOR, OPEN_PAREN, COMMA, CLOSE_PAREN, HEX_LITERAL, STRINGPARAM
    }

    inner class Token {
        var surface = ""
        var type: TokenType? = null
        var pos: Int = 0

        fun append(c: Char) {
            surface += c
        }

        fun append(s: String) {
            surface += s
        }

        fun charAt(pos: Int): Char {
            return surface[pos]
        }

        fun length(): Int {
            return surface.length
        }

        override fun toString(): String {
            return surface
        }
    }

    inner class Tokenizer
    /**
     * Creates a new tokenizer for an expression.
     *
     * @param input The expression string.
     */
    (input: String?) : Iterator<Token> {

        /**
         * Actual position in expression string.
         */
        private var pos = 0

        /**
         * The original input expression.
         */
        private val input: String
        /**
         * The previous token or `null` if none.
         */
        private var previousToken: Token? = null

        init {
            this.input = input!!.trim { it <= ' ' }
        }

        override fun hasNext(): Boolean {
            return pos < input.length
        }

        /**
         * Peek at the next character, without advancing the iterator.
         *
         * @return The next character or character 0, if at end of string.
         */
        private fun peekNextChar(): Char {
            return if (pos < input.length - 1) {
                input[pos + 1]
            } else {
                0.toChar()
            }
        }

        private fun isHexDigit(ch: Char): Boolean {
            return (ch == 'x' || ch == 'X' || ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'f'
                    || ch >= 'A' && ch <= 'F')
        }

        override fun next(): Token {
            val token = Token()

            if (pos >= input.length) {
                previousToken = null
                return previousToken!!
            }
            var ch = input[pos]
            while (Character.isWhitespace(ch) && pos < input.length) {
                ch = input[++pos]
            }
            token.pos = pos

            var isHex = false

            if (Character.isDigit(ch) || ch == decimalSeparator && Character.isDigit(peekNextChar())) {
                if (ch == '0' && (peekNextChar() == 'x' || peekNextChar() == 'X'))
                    isHex = true
                while (isHex && isHexDigit(
                                ch) || (Character.isDigit(ch) || ch == decimalSeparator || ch == 'e' || ch == 'E'
                                || (ch == minusSign && token.length() > 0
                                && ('e' == token.charAt(token.length() - 1) || 'E' == token.charAt(token.length() - 1)))
                                || (ch == '+' && token.length() > 0
                                && ('e' == token.charAt(token.length() - 1) || 'E' == token.charAt(token.length() - 1)))) && pos < input.length) {
                    token.append(input[pos++])
                    ch = if (pos == input.length) 0.toChar() else input[pos]
                }
                token.type = if (isHex) TokenType.HEX_LITERAL else TokenType.LITERAL
            } else if (ch == '"') {
                pos++
                if (previousToken!!.type !== TokenType.STRINGPARAM) {
                    ch = input[pos]
                    while (ch != '"') {
                        token.append(input[pos++])
                        ch = if (pos == input.length) 0.toChar() else input[pos]
                    }
                    token.type = TokenType.STRINGPARAM
                } else {
                    return next()
                }
            } else if (Character.isLetter(ch) || firstVarChars.indexOf(ch) >= 0) {
                while ((Character.isLetter(ch) || Character.isDigit(ch) || varChars.indexOf(ch) >= 0
                                || token.length() == 0 && firstVarChars.indexOf(ch) >= 0) && pos < input.length) {
                    token.append(input[pos++])
                    ch = if (pos == input.length) 0.toChar() else input[pos]
                }
                // Remove optional white spaces after function or variable name
                if (Character.isWhitespace(ch)) {
                    while (Character.isWhitespace(ch) && pos < input.length) {
                        ch = input[pos++]
                    }
                    pos--
                }
                token.type = if (ch == '(') TokenType.FUNCTION else TokenType.VARIABLE
            } else if (ch == '(' || ch == ')' || ch == ',') {
                if (ch == '(') {
                    token.type = TokenType.OPEN_PAREN
                } else if (ch == ')') {
                    token.type = TokenType.CLOSE_PAREN
                } else {
                    token.type = TokenType.COMMA
                }
                token.append(ch)
                pos++
            } else {
                var greedyMatch = ""
                val initialPos = pos
                ch = input[pos]
                var validOperatorSeenUntil = -1
                while (!Character.isLetter(ch) && !Character.isDigit(ch) && firstVarChars.indexOf(ch) < 0
                        && !Character.isWhitespace(ch) && ch != '(' && ch != ')' && ch != ','
                        && pos < input.length) {
                    greedyMatch += ch
                    pos++
                    if (operators.containsKey(greedyMatch)) {
                        validOperatorSeenUntil = pos
                    }
                    ch = if (pos == input.length) 0.toChar() else input[pos]
                }
                if (validOperatorSeenUntil != -1) {
                    token.append(input.substring(initialPos, validOperatorSeenUntil))
                    pos = validOperatorSeenUntil
                } else {
                    token.append(greedyMatch)
                }

                if (previousToken == null || previousToken!!.type === TokenType.OPERATOR
                        || previousToken!!.type === TokenType.OPEN_PAREN || previousToken!!.type === TokenType.COMMA) {
                    token.surface += "u"
                    token.type = TokenType.UNARY_OPERATOR
                } else {
                    token.type = TokenType.OPERATOR
                }
            }
            previousToken = token
            return previousToken as Token
        }

        fun remove() {
            throw ExpressionException("remove() not supported")
        }

    }

    private fun assertNotNull(v1: BigDecimal?) {
        if (v1 == null) {
            throw ArithmeticException("Operand may not be null")
        }
    }

    private fun assertNotNull(v1: BigDecimal?, v2: BigDecimal?) {
        if (v1 == null) {
            throw ArithmeticException("First operand may not be null")
        }
        if (v2 == null) {
            throw ArithmeticException("Second operand may not be null")
        }
    }

    private fun assertNotZero(v1: BigInteger, message: String) {
        if (v1 == BigInteger.ZERO)
            throw ArithmeticException(message)
    }

    private fun assertNotZero(v1: BigDecimal, message: String) {
        if (v1 == BigDecimal.ZERO)
            throw ArithmeticException(message)
    }

    private fun assertNotZero(v1: Int, message: String) {
        if (v1 == 0)
            throw ArithmeticException(message)
    }

    /**
     * Is the string a number?
     *
     * @param st The string.
     * @return `true`, if the input string is a number.
     */
    private fun isNumber(st: String): Boolean {
        if (st[0] == minusSign && st.length == 1)
            return false
        if (st[0] == '+' && st.length == 1)
            return false
        if (st[0] == decimalSeparator && (st.length == 1 || !Character.isDigit(st[1])))
            return false
        if (st[0] == 'e' || st[0] == 'E')
            return false
        for (ch in st.toCharArray()) {
            if (!Character.isDigit(ch) && ch != minusSign && ch != decimalSeparator && ch != 'e' && ch != 'E'
                    && ch != '+')
                return false
        }
        return true
    }

    /**
     * Creates a new expression instance from an expression string with a given
     * default match context.
     *
     * @param expression         The expression. E.g. `"2.4*sin(3)/(2-4)"` or
     * `"sin(y)>0 & max(z, 3)>3"`
     * @param defaultMathContext The [MathContext] to use by default.
     */
    init {
        this.mc = defaultMathContext
        this.expression = expression
        this.originalExpression = expression

        addOperator(object : Operator("+", OPERATOR_PRECEDENCE_ADDITIVE, true) {
            override fun eval(v1: BigDecimal?, v2: BigDecimal?): BigDecimal {
                assertNotNull(v1, v2)
                return v1!!.add(v2, mc)

            }
        })

        addOperator(object : Operator("-", OPERATOR_PRECEDENCE_ADDITIVE, true) {
            override fun eval(v1: BigDecimal?, v2: BigDecimal?): BigDecimal {
                assertNotNull(v1, v2)
                return v1!!.subtract(v2, mc)
            }
        })

        addOperator(object : Operator("*", OPERATOR_PRECEDENCE_MULTIPLICATIVE, true) {
            override fun eval(v1: BigDecimal?, v2: BigDecimal?): BigDecimal {
                assertNotNull(v1, v2)
                return v1!!.multiply(v2, mc)
            }
        })

        addOperator(object : Operator("/", OPERATOR_PRECEDENCE_MULTIPLICATIVE, true) {
            override fun eval(v1: BigDecimal?, v2: BigDecimal?): BigDecimal {
                assertNotNull(v1, v2)
                return v1!!.divide(v2, mc)
            }
        })

        addOperator(object : Operator("%", OPERATOR_PRECEDENCE_MULTIPLICATIVE, true) {
            override fun eval(v1: BigDecimal?, v2: BigDecimal?): BigDecimal {
                assertNotNull(v1, v2)
                return v1!!.remainder(v2, mc)
            }
        })

        addOperator(object : Operator("^", OPERATOR_PRECEDENCE_POWER, false) {
            override fun eval(v1: BigDecimal?, v2: BigDecimal?): BigDecimal {
                var va2 = v2
                assertNotNull(v1, va2)
                /*-
                 * Thanks to Gene Marin:
                 * http://stackoverflow.com/questions/3579779/how-to-do-a-fractional-power-on-bigdecimal-in-java
                 */
                val signOf2 = va2!!.signum()
                val dn1 = v1!!.toDouble()
                va2 = va2.multiply(BigDecimal(signOf2)) // n2 is now positive
                val remainderOf2 = va2!!.remainder(BigDecimal.ONE)
                val n2IntPart = va2.subtract(remainderOf2)
                val intPow = v1.pow(n2IntPart.intValueExact(), mc)
                val doublePow = BigDecimal(Math.pow(dn1, remainderOf2.toDouble()))

                var result = intPow.multiply(doublePow, mc)
                if (signOf2 == -1) {
                    result = BigDecimal.ONE.divide(result, mc!!.precision, RoundingMode.HALF_UP)
                }
                return result
            }
        })

        addOperator(object : Operator("&&", OPERATOR_PRECEDENCE_AND, false, true) {
            override fun eval(v1: BigDecimal?, v2: BigDecimal?): BigDecimal {
                assertNotNull(v1, v2)

                val b1 = v1!!.compareTo(BigDecimal.ZERO) != 0

                if (!b1) {
                    return BigDecimal.ZERO
                }

                val b2 = v2!!.compareTo(BigDecimal.ZERO) != 0
                return if (b2) BigDecimal.ONE else BigDecimal.ZERO
            }
        })

        addOperator(object : Operator("||", OPERATOR_PRECEDENCE_OR, false, true) {
            override fun eval(v1: BigDecimal?, v2: BigDecimal?): BigDecimal {
                assertNotNull(v1, v2)

                val b1 = v1!!.compareTo(BigDecimal.ZERO) != 0

                if (b1) {
                    return BigDecimal.ONE
                }

                val b2 = v2!!.compareTo(BigDecimal.ZERO) != 0
                return if (b2) BigDecimal.ONE else BigDecimal.ZERO
            }
        })

        addOperator(object : Operator(">", OPERATOR_PRECEDENCE_COMPARISON, false, true) {
            override fun eval(v1: BigDecimal?, v2: BigDecimal?): BigDecimal {
                assertNotNull(v1, v2)
                return if (v1!!.compareTo(v2!!) == 1) BigDecimal.ONE else BigDecimal.ZERO
            }
        })

        addOperator(object : Operator(">=", OPERATOR_PRECEDENCE_COMPARISON, false, true) {
            override fun eval(v1: BigDecimal?, v2: BigDecimal?): BigDecimal {
                assertNotNull(v1, v2)
                return if (v1!!.compareTo(v2!!) >= 0) BigDecimal.ONE else BigDecimal.ZERO
            }
        })

        addOperator(object : Operator("<", OPERATOR_PRECEDENCE_COMPARISON, false, true) {
            override fun eval(v1: BigDecimal?, v2: BigDecimal?): BigDecimal {
                assertNotNull(v1, v2)
                return if (v1!!.compareTo(v2!!) == -1) BigDecimal.ONE else BigDecimal.ZERO
            }
        })

        addOperator(object : Operator("<=", OPERATOR_PRECEDENCE_COMPARISON, false, true) {
            override fun eval(v1: BigDecimal?, v2: BigDecimal?): BigDecimal {
                assertNotNull(v1, v2)
                return if (v1!!.compareTo(v2!!) <= 0) BigDecimal.ONE else BigDecimal.ZERO
            }
        })

        addOperator(object : Operator("=", OPERATOR_PRECEDENCE_EQUALITY, false, true) {
            override fun eval(v1: BigDecimal?, v2: BigDecimal?): BigDecimal {
                if (v1 === v2) {
                    return BigDecimal.ONE
                }
                if (v1 == null || v2 == null) {
                    return BigDecimal.ZERO
                }
                return if (v1.compareTo(v2) == 0) BigDecimal.ONE else BigDecimal.ZERO
            }
        })

        addOperator(object : Operator("==", OPERATOR_PRECEDENCE_EQUALITY, false, true) {
            override fun eval(v1: BigDecimal?, v2: BigDecimal?): BigDecimal {
                return (operators["="] as Operator).eval(v1, v2)
            }
        })

        addOperator(object : Operator("!=", OPERATOR_PRECEDENCE_EQUALITY, false, true) {
            override fun eval(v1: BigDecimal?, v2: BigDecimal?): BigDecimal {
                if (v1 === v2) {
                    return BigDecimal.ZERO
                }
                if (v2 == null) {
                    return BigDecimal.ONE
                }
                return if (v1!!.compareTo(v2) != 0) BigDecimal.ONE else BigDecimal.ZERO
            }
        })

        addOperator(object : Operator("<>", OPERATOR_PRECEDENCE_EQUALITY, false, true) {
            override fun eval(v1: BigDecimal?, v2: BigDecimal?): BigDecimal {
                assertNotNull(v1, v2)
                return (operators["!="] as Operator).eval(v1, v2)
            }
        })

        addOperator(object : UnaryOperator("-", OPERATOR_PRECEDENCE_UNARY, false) {
            override fun evalUnary(v1: BigDecimal?): BigDecimal {
                return v1!!.multiply(BigDecimal(-1))
            }
        })

        addOperator(object : UnaryOperator("+", OPERATOR_PRECEDENCE_UNARY, false) {
            override fun evalUnary(v1: BigDecimal?): BigDecimal {
                return v1!!.multiply(BigDecimal.ONE)
            }
        })

        addFunction(object : Function("FACT", 1, false) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                val number = parameters[0]!!.toInt()
                var factorial = BigDecimal.ONE
                for (i in 1..number) {
                    factorial = factorial.multiply(BigDecimal(i))
                }
                return factorial
            }
        })

        addLazyFunction(object : AbstractLazyFunction("SUM", 1) {
            override fun lazyEval(lazyParams: List<LazyNumber>): LazyNumber {
                val valueList = getBigDecimalParams(lazyParams[0])
                val result = valueList.stream().reduce(BigDecimal.ZERO, { obj, augend -> obj.add(augend) })
                return object : LazyNumber {
                    override val string: String?
                        get() = result.toPlainString()

                    override fun eval(): BigDecimal? {
                        return result
                    }
                }
            }
        })

        addLazyFunction(object : AbstractLazyFunction("MEAN", 1) {
            override fun lazyEval(lazyParams: List<LazyNumber>): LazyNumber {
                val valueList = getBigDecimalParams(lazyParams[0])
                val divisor = valueList.size.toBigDecimal()
                assertNotZero(divisor, "Array length cannot be zero")
                val e = Expression("SUM(X)", mc).where("X", valueList).eval()
                val result = e!!.divide(divisor)
                return object : LazyNumber {
                    override val string: String?
                        get() = result.toPlainString()

                    override fun eval(): BigDecimal? {
                        return result
                    }
                }
            }
        })

        addLazyFunction(object : AbstractLazyFunction("VARIANCE", 1) {
            override fun lazyEval(lazyParams: List<LazyNumber>): LazyNumber {
                val valueList = getBigDecimalParams(lazyParams[0])
                val divisor = valueList.size.toBigDecimal()
                assertNotZero(divisor, "Array length cannot be zero")
                // get the mean
                val mean = Expression("MEAN(X)", mc).where("X", valueList).eval()
                // get the differences of the mean to each data point and square them
                var differences = ArrayList<BigDecimal>()
                for (i in valueList) {
                    differences.add((i.minus(mean!!)).pow(2,mc))
                }
                // get the mean of that. this is the result
                val result = Expression("MEAN(X)", mc).where("X", differences).eval()
                return object : LazyNumber {
                    override val string: String?
                        get() = result?.toPlainString()

                    override fun eval(): BigDecimal? {
                        return result
                    }
                }
            }
        })

        addLazyFunction(object : AbstractLazyFunction("STDDEV", 1) {
            override fun lazyEval(lazyParams: List<LazyNumber>): LazyNumber {
                val valueList = getBigDecimalParams(lazyParams[0])
                val divisor = valueList.size.toBigDecimal()
                assertNotZero(divisor, "Array length cannot be zero")
                val variance = Expression("VARIANCE(X)", mc).where("X", valueList).eval()

                val result = Expression("SQRT(X)", mc).where("X", variance!!).eval()
                return object : LazyNumber {
                    override val string: String?
                        get() = result?.toPlainString()

                    override fun eval(): BigDecimal? {
                        return result
                    }
                }
            }
        })


        addFunction(object : Function("NOT", 1, true) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                val zero = parameters[0]!!.compareTo(BigDecimal.ZERO) == 0
                return if (zero) BigDecimal.ONE else BigDecimal.ZERO
            }
        })

        addLazyFunction(object : LazyFunction("IF", 3) {
            override fun lazyEval(lazyParams: List<LazyNumber>): LazyNumber {
                val result = lazyParams[0].eval()
                assertNotNull(result)
                val isTrue = result!!.compareTo(BigDecimal.ZERO) != 0
                return if (isTrue) lazyParams[1] else lazyParams[2]
            }
        })

        addFunction(object : Function("RANDOM", 0) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                val d = Math.random()
                return BigDecimal(d, mc)
            }
        })

        addFunction(object : Function("SIN", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                val d = Math.sin(Math.toRadians(parameters[0]!!.toDouble()))
                return BigDecimal(d, mc)
            }
        })

        addFunction(object : Function("COS", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                val d = Math.cos(Math.toRadians(parameters[0]!!.toDouble()))
                return BigDecimal(d, mc)
            }
        })

        addFunction(object : Function("TAN", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                val d = Math.tan(Math.toRadians(parameters[0]!!.toDouble()))
                return BigDecimal(d, mc)
            }
        })

        addFunction(object : Function("ASIN", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                val d = Math.toDegrees(Math.asin(parameters[0]!!.toDouble()))
                return BigDecimal(d, mc)
            }
        })

        addFunction(object : Function("ACOS", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                val d = Math.toDegrees(Math.acos(parameters[0]!!.toDouble()))
                return BigDecimal(d, mc)
            }
        })

        addFunction(object : Function("ATAN", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                val d = Math.toDegrees(Math.atan(parameters[0]!!.toDouble()))
                return BigDecimal(d, mc)
            }
        })

        addFunction(object : Function("ATAN2", 2) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0], parameters[1])
                val d = Math.toDegrees(Math.atan2(parameters[0]!!.toDouble(), parameters[1]!!.toDouble()))
                return BigDecimal(d, mc)
            }
        })

        addFunction(object : Function("SINH", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                val d = Math.sinh(parameters[0]!!.toDouble())
                return BigDecimal(d, mc)
            }
        })

        addFunction(object : Function("COSH", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                val d = Math.cosh(parameters[0]!!.toDouble())
                return BigDecimal(d, mc)
            }
        })

        addFunction(object : Function("TANH", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                val d = Math.tanh(parameters[0]!!.toDouble())
                return BigDecimal(d, mc)
            }
        })

        addFunction(object : Function("SEC", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                /** Formula: sec(x) = 1 / cos(x)  */
                val one = 1.0
                val d = Math.cos(Math.toRadians(parameters[0]!!.toDouble()))
                return BigDecimal(one / d, mc)
            }
        })

        addFunction(object : Function("CSC", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                /** Formula: csc(x) = 1 / sin(x)  */
                val one = 1.0
                val d = Math.sin(Math.toRadians(parameters[0]!!.toDouble()))
                return BigDecimal(one / d, mc)
            }
        })

        addFunction(object : Function("SECH", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                /** Formula: sech(x) = 1 / cosh(x)  */
                val one = 1.0
                val d = Math.cosh(parameters[0]!!.toDouble())
                return BigDecimal(one / d, mc)
            }
        })

        addFunction(object : Function("CSCH", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                /** Formula: csch(x) = 1 / sinh(x)  */
                val one = 1.0
                val d = Math.sinh(parameters[0]!!.toDouble())
                return BigDecimal(one / d, mc)
            }
        })

        addFunction(object : Function("COT", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                /** Formula: cot(x) = cos(x) / sin(x) = 1 / tan(x)  */
                val one = 1.0
                val d = Math.tan(Math.toRadians(parameters[0]!!.toDouble()))
                return BigDecimal(one / d, mc)
            }
        })

        addFunction(object : Function("ACOT", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                /** Formula: acot(x) = atan(1/x)  */
                if (parameters[0]!!.toDouble() == 0.0) {
                    throw ExpressionException("Number must not be 0")
                }
                val d = Math.toDegrees(Math.atan(1 / parameters[0]!!.toDouble()))
                return BigDecimal(d, mc)
            }
        })

        addFunction(object : Function("COTH", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                /** Formula: coth(x) = 1 / tanh(x)  */
                val one = 1.0
                val d = Math.tanh(parameters[0]!!.toDouble())
                return BigDecimal(one / d, mc)
            }
        })

        addFunction(object : Function("ASINH", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                /** Formula: asinh(x) = ln(x + sqrt(x^2 + 1))  */
                val d = Math.log(parameters[0]!!.toDouble() + Math.sqrt(Math.pow(parameters[0]!!.toDouble(), 2.0) + 1))
                return BigDecimal(d, mc)
            }
        })

        addFunction(object : Function("ACOSH", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                /** Formula: acosh(x) = ln(x + sqrt(x^2 - 1))  */
                if (java.lang.Double.compare(parameters[0]!!.toDouble(), 1.0) < 0) {
                    throw ExpressionException("Number must be x >= 1")
                }
                val d = Math.log(parameters[0]!!.toDouble() + Math.sqrt(Math.pow(parameters[0]!!.toDouble(), 2.0) - 1))
                return BigDecimal(d, mc)
            }
        })

        addFunction(object : Function("ATANH", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                /** Formula: atanh(x) = 0.5*ln((1 + x)/(1 - x))  */
                if (Math.abs(parameters[0]!!.toDouble()) > 1 || Math.abs(parameters[0]!!.toDouble()) == 1.0) {
                    throw ExpressionException("Number must be |x| < 1")
                }
                val d = 0.5 * Math.log((1 + parameters[0]!!.toDouble()) / (1 - parameters[0]!!.toDouble()))
                return BigDecimal(d, mc)
            }
        })

        addFunction(object : Function("RAD", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                val d = Math.toRadians(parameters[0]!!.toDouble())
                return BigDecimal(d, mc)
            }
        })

        addFunction(object : Function("DEG", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                val d = Math.toDegrees(parameters[0]!!.toDouble())
                return BigDecimal(d, mc)
            }
        })

        addFunction(object : Function("MAX", -1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                if (parameters.isEmpty()) {
                    throw ExpressionException("MAX requires at least one parameter")
                }
                var max: BigDecimal? = null
                for (parameter in parameters) {
                    assertNotNull(parameter)
                    if (max == null || parameter!! > max) {
                        max = parameter
                    }
                }
                return max!!
            }
        })

        addFunction(object : Function("MIN", -1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                if (parameters.isEmpty()) {
                    throw ExpressionException("MIN requires at least one parameter")
                }
                var min: BigDecimal? = null
                for (parameter in parameters) {
                    assertNotNull(parameter)
                    if (min == null || parameter!! < min) {
                        min = parameter
                    }
                }
                return min!!
            }
        })

        addFunction(object : Function("ABS", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                return parameters[0]!!.abs(mc)
            }
        })

        addFunction(object : Function("LOG", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                val d = Math.log(parameters[0]!!.toDouble())
                return BigDecimal(d, mc)
            }
        })

        addFunction(object : Function("LOG10", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                val d = Math.log10(parameters[0]!!.toDouble())
                return BigDecimal(d, mc)
            }
        })

        addFunction(object : Function("ROUND", 2) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0], parameters[1])
                val toRound = parameters[0]
                val precision = parameters[1]!!.toInt()
                return toRound!!.setScale(precision, mc?.roundingMode)
            }
        })

        addFunction(object : Function("FLOOR", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                val toRound = parameters[0]
                return toRound!!.setScale(0, RoundingMode.FLOOR)
            }
        })

        addFunction(object : Function("CEILING", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                val toRound = parameters[0]
                return toRound!!.setScale(0, RoundingMode.CEILING)
            }
        })

        addFunction(object : Function("SQRT", 1) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0])
                val x = parameters[0]
                if (x!!.compareTo(BigDecimal.ZERO) == 0) {
                    return BigDecimal(0)
                }
                if (x.signum() < 0) {
                    throw ExpressionException("Argument to SQRT() function must not be negative")
                }
                val mathContext = MathContext(mc!!.precision.times(2), RoundingMode.HALF_DOWN)
                val maxPrecision = mathContext.precision + 6
                val acceptableError = BigDecimal.ONE.movePointLeft(mathContext.precision + 1)
                var result: BigDecimal
                if (isDoubleValue(x)) {
                    result = BigDecimal.valueOf(Math.sqrt(x.toDouble()))
                } else {
                    result = x.divide(TWO, mc)
                }

                if (result.multiply(result, mc).compareTo(x) == 0) {
                    return result.setScale(mc!!.precision, mc!!.roundingMode) // early exit if x is a square number
                }

                var adaptivePrecision = mathContext.precision
                var last: BigDecimal

                do {
                    last = result
                    adaptivePrecision = adaptivePrecision.times(2)
                    if (adaptivePrecision > maxPrecision) {
                        adaptivePrecision = maxPrecision
                    }
                    val mac = MathContext(adaptivePrecision, RoundingMode.HALF_DOWN)
                    result = x.divide(result, mac).add(last, mac).divide(TWO, mac)
                } while (adaptivePrecision < maxPrecision || result.subtract(last).abs() > acceptableError)

                return result.setScale(mc!!.precision, mc!!.roundingMode)
            }
        })

        addFunction(object : Function("ROOTN", 2) {
            override fun eval(parameters: MutableList<BigDecimal?>): BigDecimal {
                assertNotNull(parameters[0], parameters[1])
                val x = parameters[0]
                val n = parameters[1]
                val mathContext = MathContext(mc!!.precision * 2 ,RoundingMode.HALF_UP)

                val maxPrecision = mathContext.precision + 6
                val acceptableError = BigDecimal.ONE.movePointLeft(mathContext.precision - 1)
                if (x!!.compareTo(BigDecimal.ZERO) == 0) {
                    return BigDecimal(0)
                }
                if (x.signum() < 0) {
                    throw ArithmeticException("First argument for ROOTN(X,Y) must not be negative")
                }
                if (n!! <= BigDecimal.ONE) {
                    val mac = MathContext(mathContext.precision +6, mathContext.roundingMode)

                    val result = Expression("X^(1/Y)", mac).where("X", x).where("Y", n).eval()
                    return result!!.setScale(mc!!.precision, mc!!.roundingMode)
                }
                val nMinus1 = n.minus(ONE)
                var result = x.divide(TWO, mathContext)
                var adaptivePrecision = 2
                do {
                    adaptivePrecision = adaptivePrecision.times(3)
                    if (adaptivePrecision > maxPrecision) {
                        adaptivePrecision = maxPrecision
                    }
                    val mac = MathContext(adaptivePrecision, RoundingMode.HALF_UP)

                    val power = Expression("X^Y", mac).where("X", result).where("Y",nMinus1).eval()
                    var step = x.divide(power, mac).subtract(result, mac).divide(n,mac)
                    result = result.add(step, mathContext)
                } while ( step.abs() > acceptableError)

                return result.setScale(mc!!.precision, mc!!.roundingMode)
            }
        })


        variables["e"] = createLazyNumber(e)
        variables["PI"] = createLazyNumber(PI)
        variables["PHI"] = createLazyNumber(PHI)
        variables["sq2"] = createLazyNumber(sq2)
        variables["sq3"] = createLazyNumber(sq3)
        variables["sq5"] = createLazyNumber(sq5)
        variables["NULL"] = null
        variables["TRUE"] = createLazyNumber(BigDecimal.ONE)
        variables["FALSE"] = createLazyNumber(BigDecimal.ZERO)

    }

    fun isDoubleValue(value: BigDecimal): Boolean {
        if (value > DOUBLE_MAX_VALUE) {
            return false
        }
        return value >= DOUBLE_MAX_VALUE.negate()

    }
    /**
     * Adds an operator to the list of supported operators.
     *
     * @param operator The operator to add.
     * @return The previous operator with that name, or `null` if
     * there was none.
     */
    fun <OPERATOR : LazyOperator> addOperator(operator: OPERATOR): OPERATOR? {
        var key = operator.oper
        if (operator is AbstractUnaryOperator) {
            key += "u"
        }
        @Suppress("UNCHECKED_CAST")
        return operators.put(key, operator) as? OPERATOR
    }

    /**
     * Adds a function to the list of supported functions
     *
     * @param function The function to add.
     * @return The previous operator with that name, or `null` if
     * there was none.
     */
    fun addFunction(function: com.designingknights.expressions.Function): com.designingknights.expressions.Function? {
        return functions.put(function.name, function) as? com.designingknights.expressions.Function
    }

    /**
     * Adds a lazy function function to the list of supported functions
     *
     * @param function The function to add.
     * @return The previous operator with that name, or `null` if
     * there was none.
     */
    fun addLazyFunction(function: com.designingknights.expressions.LazyFunction): com.designingknights.expressions.LazyFunction? {
        return functions.put(function.name, function)
    }

    /**
     * Sets a variable value.
     *
     * @param variable The variable name.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    fun setVariable(variable: String, value: BigDecimal): Expression {
        return setVariable(variable, createLazyNumber(value))
    }

    /**
     * Sets a variable value.
     *
     * @param variable The variable name.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    fun setVariable(variable: String, value: LazyNumber): Expression {
        variables[variable] = value
        return this
    }

    /**
     * Sets a variable value.
     *
     * @param variable The variable to set.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    fun setVariable(variable: String, value: String): Expression {
        when {
            isNumber(value) -> variables[variable] = createLazyNumber(BigDecimal(value, mc))
            value.equals("null", ignoreCase = true) -> variables[variable] = null
            else -> {
                variables[variable] = object : LazyNumber {
                    private val outerVariables = variables
                    private val outerFunctions = functions
                    private val outerOperators = operators
                    override val string: String? = value
                    private val inneMc = mc

                    override fun eval(): BigDecimal? {
                        val innerE: Expression = com.designingknights.expressions.Expression(string, inneMc)

                        innerE.variables = outerVariables
                        innerE.functions = outerFunctions
                        innerE.operators = outerOperators
                        return innerE.eval()
                    }
                }
                rpn = null
            }
        }
        return this
    }

    private fun getBigDecimalParams(lazyParams: LazyNumber): List<BigDecimal> {
        val string = lazyParams.string
        val value = variables[string]!!.string
        val tempList = value!!.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        val list = ArrayList<BigDecimal>()
        for (v in tempList) {
            list.add(BigDecimal(v.trim({ it <= ' ' })))
        }
        return list
    }

    private fun shuntingYard(expression: String): List<Token> {
        val outputQueue = ArrayList<Token>()
        val stack = Stack<Token>()

        val tokenizer = Tokenizer(expression)

        var lastFunction: Token? = null
        var previousToken: Token? = null
        while (tokenizer.hasNext()) {
            val token = tokenizer.next()
            when (token.type) {
                TokenType.STRINGPARAM -> stack.push(token)
                TokenType.LITERAL, TokenType.HEX_LITERAL -> outputQueue.add(token)
                TokenType.VARIABLE -> outputQueue.add(token)
                TokenType.FUNCTION -> {
                    stack.push(token)
                    lastFunction = token
                }
                TokenType.COMMA -> {
                    if (previousToken != null && previousToken.type === TokenType.OPERATOR) {
                        throw ExpressionException("Missing parameter(s) for operator " + previousToken
                                + " at character position " + previousToken.pos)
                    }
                    while (!stack.isEmpty() && stack.peek().type !== TokenType.OPEN_PAREN) {
                        outputQueue.add(stack.pop())
                    }
                    if (stack.isEmpty()) {
                        if (lastFunction == null) {
                            throw ExpressionException("Unexpected comma at character position " + token.pos)
                        } else {
                            throw ExpressionException(
                                    "Parse error for function '" + lastFunction + "' at character position " + token.pos)
                        }
                    }
                }
                TokenType.OPERATOR -> {
                    if (previousToken != null && (previousToken.type === TokenType.COMMA || previousToken.type === TokenType.OPEN_PAREN)) {
                        throw ExpressionException(
                                "Missing parameter(s) for operator " + token + " at character position " + token.pos)
                    }
                    val o1 = operators[token.surface]
                            ?: throw ExpressionException("Unknown operator '" + token + "' at position " + (token.pos + 1))

                    shuntOperators(outputQueue, stack, o1)
                    stack.push(token)
                }
                TokenType.UNARY_OPERATOR -> {
                    if (previousToken != null && previousToken.type !== TokenType.OPERATOR
                            && previousToken.type !== TokenType.COMMA && previousToken.type !== TokenType.OPEN_PAREN) {
                        throw ExpressionException(
                                "Invalid position for unary operator " + token + " at character position " + token.pos)
                    }
                    val o1 = operators[token.surface] ?: throw ExpressionException(
                            "Unknown unary operator '" + token.surface.substring(0, token.surface.length - 1)
                                    + "' at position " + (token.pos + 1))

                    shuntOperators(outputQueue, stack, o1)
                    stack.push(token)
                }
                TokenType.OPEN_PAREN -> {
                    if (previousToken != null) {
                        if (previousToken.type === TokenType.LITERAL || previousToken.type === TokenType.CLOSE_PAREN
                                || previousToken.type === TokenType.VARIABLE
                                || previousToken.type === TokenType.HEX_LITERAL) {
                            // Implicit multiplication, e.g. 23(a+b) or (a+b)(a-b)
                            val multiplication = Token()
                            multiplication.append("*")
                            multiplication.type = TokenType.OPERATOR
                            stack.push(multiplication)
                        }
                        // if the ( is preceded by a valid function, then it
                        // denotes the start of a parameter list
                        if (previousToken.type === TokenType.FUNCTION) {
                            outputQueue.add(token)
                        }
                    }
                    stack.push(token)
                }
                TokenType.CLOSE_PAREN -> {
                    if (previousToken != null && previousToken.type === TokenType.OPERATOR) {
                        throw ExpressionException("Missing parameter(s) for operator " + previousToken
                                + " at character position " + previousToken.pos)
                    }
                    while (!stack.isEmpty() && stack.peek().type !== TokenType.OPEN_PAREN) {
                        outputQueue.add(stack.pop())
                    }
                    if (stack.isEmpty()) {
                        throw ExpressionException("Mismatched parentheses")
                    }
                    stack.pop()
                    if (!stack.isEmpty() && stack.peek().type === TokenType.FUNCTION) {
                        outputQueue.add(stack.pop())
                    }
                }
            }
            previousToken = token
        }

        while (!stack.isEmpty()) {
            val element = stack.pop()
            if (element.type === TokenType.OPEN_PAREN || element.type === TokenType.CLOSE_PAREN) {
                throw ExpressionException("Mismatched parentheses")
            }
            outputQueue.add(element)
        }
        return outputQueue
    }

    private fun shuntOperators(outputQueue: MutableList<Token>, stack: Stack<Token>, o1: LazyOperator) {
        var nextToken: Expression.Token? = if (stack.isEmpty()) null else stack.peek()
        while (nextToken != null
                && (nextToken.type === Expression.TokenType.OPERATOR || nextToken.type === Expression.TokenType.UNARY_OPERATOR)
                && (o1.isLeftAssoc && o1.precedence <= operators[nextToken.surface]!!.precedence || o1.precedence < operators[nextToken.surface]!!.precedence)) {
            outputQueue.add(stack.pop())
            nextToken = if (stack.isEmpty()) null else stack.peek()
        }
    }

    /**
     * Evaluates the expression.
     *
     * @return The result of the expression. Trailing zeros are stripped.
     */
    fun eval(): BigDecimal? {
        return eval(true)
    }

    /**
     * Evaluates the expression.
     *
     * @param stripTrailingZeros If set to `true` trailing zeros in the result are
     * stripped.
     * @return The result of the expression.
     */
    fun eval(stripTrailingZeros: Boolean): BigDecimal? {

        val stack = Stack<LazyNumber>()

        for (token in getRPN()) {
            when (token.type) {
                TokenType.UNARY_OPERATOR -> {
                    val value = stack.pop()
                    val result = object : LazyNumber {

                        override val string: String?
                            get() = operators[token.surface]?.eval(value, null)?.eval().toString()

                        override fun eval(): BigDecimal? {
                            return operators[token.surface]?.eval(value, null)?.eval()
                        }
                    }
                    stack.push(result)
                }
                TokenType.OPERATOR -> {
                    val v1 = stack.pop()
                    val v2 = stack.pop()
                    val result = object : LazyNumber {

                        override val string: String?
                            get() = operators[token.surface]?.eval(v2, v1)?.eval().toString()

                        override fun eval(): BigDecimal? {
                            return operators[token.surface]?.eval(v2, v1)?.eval()
                        }
                    }
                    stack.push(result)
                }
                TokenType.VARIABLE -> {
                    if (!variables.containsKey(token.surface)) {
                        throw ExpressionException("Unknown operator or function: $token")
                    }

                    stack.push(object : LazyNumber {

                        override val string: String?
                            get() = token.surface

                        override fun eval(): BigDecimal? {
                            val lazyVariable = variables[token.surface]
                            val value = lazyVariable?.eval()
                            return value?.round(mc)
                        }
                    })
                }
                TokenType.FUNCTION -> {
                    val f = functions[token.surface.toUpperCase(Locale.ROOT)]
                    val p = ArrayList<LazyNumber>(if (!f!!.numParamsVaries()) f.numParams else 0)
                    // pop parameters off the stack until we hit the start of
                    // this function's parameter list
                    while (!stack.isEmpty() && stack.peek() !== PARAMS_START) {
                        p.add(0, stack.pop())
                    }

                    if (stack.peek() === PARAMS_START) {
                        stack.pop()
                    }

                    val fResult = f.lazyEval(p)
                    stack.push(fResult)
                }
                TokenType.OPEN_PAREN -> stack.push(PARAMS_START)
                TokenType.LITERAL -> stack.push(object : LazyNumber {

                    override val string: String?
                        get() = BigDecimal(token.surface, mc).toString()

                    override fun eval(): BigDecimal? {
                        return if (token.surface.equals("NULL", true)) {
                            null
                        } else BigDecimal(token.surface, mc)

                    }
                })
                TokenType.STRINGPARAM -> stack.push(object : LazyNumber {

                    override val string: String?
                        get() = token.surface

                    override fun eval(): BigDecimal? {
                        return null
                    }
                })
                TokenType.HEX_LITERAL -> stack.push(object : LazyNumber {

                    override val string: String?
                        get() = BigInteger(token.surface.substring(2), 16).toString()

                    override fun eval(): BigDecimal? {
                        return BigDecimal(BigInteger(token.surface.substring(2), 16), mc)
                    }
                })
                else -> throw ExpressionException(
                        "Unexpected token '" + token.surface + "' at character position " + token.pos)
            }
        }
        val result = stack.pop().eval()
        return if (result == null) null else if (stripTrailingZeros) result.stripTrailingZeros() else result
    }

    /**
     * Cached access to the RPN notation of this expression, ensures only one
     * calculation of the RPN per expression instance. If no cached instance
     * exists, a new one will be created and put to the cache.
     *
     * @return The cached RPN instance.
     */
    private fun getRPN(): List<Token> {
        if (rpn == null) {
            rpn = shuntingYard(this.expression!!)
            validate(rpn!!)
        }
        return rpn as List<Token>
    }

    /**
     * Get an iterator for this expression, allows iterating over an expression
     * token by token.
     *
     * @return A new iterator instance for this expression.
     */
    fun getExpressionTokenizer(): Iterator<Token> {
        val expression = this.expression

        return Tokenizer(expression!!)
    }

    /**
     * Check that the expression has enough numbers and variables to fit the
     * requirements of the operators and functions, also check for only 1 result
     * stored at the end of the evaluation.
     */
    private fun validate(rpn: List<Token>) {
        /*-
         * Thanks to Norman Ramsey:
         * http://http://stackoverflow.com/questions/789847/postfix-notation-validation
         */
        // each push on to this stack is a new function scope, with the value of
        // each
        // layer on the stack being the count of the number of parameters in
        // that scope
        val stack = Stack<Int>()

        // push the 'global' scope
        stack.push(0)

        for (token in rpn) {
            when (token.type) {
                TokenType.UNARY_OPERATOR -> if (stack.peek() < 1) {
                    throw ExpressionException("Missing parameter(s) for operator $token")
                }
                TokenType.OPERATOR -> {
                    if (stack.peek() < 2) {
                        throw ExpressionException("Missing parameter(s) for operator $token")
                    }
                    // pop the operator's 2 parameters and add the result
                    stack[stack.size - 1] = stack.peek() - 2 + 1
                }
                TokenType.FUNCTION -> {
                    val f = functions[token.surface.toUpperCase(Locale.ROOT)]
                            ?: throw ExpressionException("Unknown function '" + token + "' at position " + (token.pos + 1))

                    val numParams = stack.pop()
                    if (!f.numParamsVaries() && numParams != f.numParams) {
                        throw ExpressionException(
                                "Function " + token + " expected " + f.numParams + " parameters, got " + numParams)
                    }
                    if (stack.size <= 0) {
                        throw ExpressionException("Too many function calls, maximum scope exceeded")
                    }
                    // push the result of the function
                    stack[stack.size - 1] = stack.peek() + 1
                }
                TokenType.OPEN_PAREN -> stack.push(0)
                else -> stack[stack.size - 1] = stack.peek() + 1
            }
        }

        if (stack.size > 1) {
            throw ExpressionException("Too many unhandled function parameter lists")
        } else if (stack.peek() > 1) {
            throw ExpressionException("Too many numbers or variables")
        } else if (stack.peek() < 1) {
            throw ExpressionException("Empty expression")
        }
    }

    /**
     * Sets the precision for expression evaluation.
     *
     * @param precision The new precision.
     * @return The expression, allows to chain methods.
     */
    fun setPrecision(precision: Int): Expression {
        this.mc = MathContext(precision)
        return this
    }

    /**
     * Sets the rounding mode for expression evaluation.
     *
     * @param roundingMode The new rounding mode.
     * @return The expression, allows to chain methods.
     */
    fun setRoundingMode(roundingMode: RoundingMode): Expression {
        this.mc = MathContext(mc!!.precision, roundingMode)
        return this
    }

    /**
     * Sets the characters other than letters and digits that are valid as the
     * first character of a variable.
     *
     * @param chars The new set of variable characters.
     * @return The expression, allows to chain methods.
     */
    fun setFirstVariableCharacters(chars: String): Expression {
        this.firstVarChars = chars
        return this
    }

    /**
     * Sets the characters other than letters and digits that are valid as the
     * second and subsequent characters of a variable.
     *
     * @param chars The new set of variable characters.
     * @return The expression, allows to chain methods.
     */
    fun setVariableCharacters(chars: String): Expression {
        this.varChars = chars
        return this
    }

    /**
     * Creates a new inner expression for nested expression.
     *
     * @param expression The string expression.
     * @return The inner Expression instance.
     */
    private fun createEmbeddedExpression(expression: String?): Expression {
        val outerVariables = variables
        val outerFunctions = functions
        val outerOperators = operators
        val inneMc = mc
        val exp = Expression(expression, inneMc)
        exp.variables = outerVariables
        exp.functions = outerFunctions
        exp.operators = outerOperators
        return exp
    }

    /**
     * Sets a variable value.
     *
     * @param variable The variable to set.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    fun where(variable: String, value: BigDecimal): Expression {
        return setVariable(variable, value)
    }

    /**
     * Sets a variable value.
     *
     * @param variable The variable to set.
     * @param values    The variable value.
     * @return The expression, allows to chain methods.
     */
    fun where(variable: String, values: Array<BigDecimal>): Expression {

        val temp = arrayOfNulls<String>(values.size)
        for (i in values.indices) {
            temp[i] = values[i].toString()
        }
        val stringList = temp.joinToString(",")
        return setVariable(variable, stringList)

    }

    /**
     * Sets a variable value.
     *
     * @param variable The variable to set.
     * @param values   The variable values.
     * @return The expression, allows to chain methods.
     */

    fun where(variable: String, values: List<BigDecimal>): Expression {
        var stringList = values.toString()
        stringList = stringList.replace("[", "")
        stringList = stringList.replace("]", "")

        return setVariable(variable, stringList)
    }

    /**
     * Sets a variable value.
     *
     * @param variable The variable to set.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    fun where(variable: String, value: LazyNumber): Expression {
        return setVariable(variable, value)
    }


    /**
     * Sets a variable value.
     *
     * @param variable The variable to set.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    fun where(variable: String, value: String): Expression {
        return setVariable(variable, value)
    }

    /**
     * Get a string representation of the RPN (Reverse Polish Notation) for this
     * expression.
     *
     * @return A string with the RPN representation for this expression.
     */
    fun toRPN(): String {
        val result = StringBuilder()
        for (t in getRPN()) {
            if (result.length != 0)
                result.append(" ")
            if (t.type === TokenType.VARIABLE && variables.containsKey(t.surface)) {
                val innerVariable = variables[t.surface]
                val innerExp = innerVariable!!.string
                if (isNumber(innerExp!!)) { // if it is a number, then we don't
                    // expan in the RPN
                    result.append(t.toString())
                } else { // expand the nested variable to its RPN representation
                    val exp = createEmbeddedExpression(innerExp)
                    val nestedExpRpn = exp.toRPN()
                    result.append(nestedExpRpn)
                }
            } else {
                result.append(t.toString())
            }
        }
        return result.toString()
    }

    /**
     * Exposing declared variables in the expression.
     *
     * @return All declared variables.
     */
    fun getDeclaredVariables(): Set<String> {
        return Collections.unmodifiableSet(variables.keys)
    }

    /**
     * Exposing declared operators in the expression.
     *
     * @return All declared operators.
     */
    fun getDeclaredOperators(): Set<String> {
        return Collections.unmodifiableSet(operators.keys)
    }

    /**
     * Exposing declared functions.
     *
     * @return All declared functions.
     */
    fun getDeclaredFunctions(): Set<String> {
        return Collections.unmodifiableSet(functions.keys)
    }

    /**
     * @return The original expression string
     */
    fun getExpression(): String? {
        return expression
    }


    /**
     * Returns a list of the variables in the expression.
     *
     * @return A list of the variable names in this expression.
     */
    fun getUsedVariables(): List<String> {
        val result = ArrayList<String>()
        val tokenizer = Tokenizer(expression)
        while (tokenizer.hasNext()) {
            val nextToken = tokenizer.next()
            val token = nextToken.toString()
            if (nextToken.type !== TokenType.VARIABLE || token == "PI" || token == "e" || token == "TRUE"
                    || token == "FALSE") {
                continue
            }
            result.add(token)
        }
        return result
    }

    /**
     * The original expression used to construct this expression, without
     * variables substituted.
     */
    fun getOriginalExpression(): String? {
        return this.originalExpression
    }

    /**
     * {@inheritDoc}
     */
    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null || javaClass != other.javaClass)
            return false
        val that = other as Expression?
        return if (this.expression == null) {
            that!!.expression == null
        } else {
            this.expression.equals(that!!.expression)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun hashCode(): Int {
        return if (this.expression == null) 0 else this.expression!!.hashCode()
    }

    /**
     * {@inheritDoc}
     */
    override fun toString(): String {
        return this.expression!!
    }

    /**
     * Checks whether the expression is a boolean expression. An expression is
     * considered a boolean expression, if the last operator or function is
     * boolean. The IF function is handled special. If the third parameter is
     * boolean, then the IF is also considered boolean, else non-boolean.
     *
     * @return `true` if the last operator/function was a boolean.
     */
    fun isBoolean(): Boolean {
        val rpn = getRPN()
        if (rpn.size > 0) {
            for (i in rpn.indices.reversed()) {
                val t = rpn[i]
                /*
                 * The IF function is handled special. If the third parameter is
                 * boolean, then the IF is also considered a boolean. Just skip
                 * the IF function to check the second parameter.
                 */
                if (t.surface.equals("IF"))
                    continue
                if (t.type === TokenType.FUNCTION) {
                    return functions[t.surface]!!.isBooleanFunction
                } else if (t.type === TokenType.OPERATOR) {
                    return operators[t.surface]!!.isBooleanOperator
                }
            }
        }
        return false
    }


}
