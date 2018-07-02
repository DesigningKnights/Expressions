package com.designingknights.expressions



import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Test
import java.math.MathContext
import java.math.RoundingMode



class TestEvals {

    @Test
    fun testsinAB() {
        var err = ""
        try {
            val expression = Expression("sin(a+x)")
            expression.eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("Unknown operator or function: a", err)
    }

    @Test
    fun testInvalidExpressions1() {
        var err = ""
        try {
            val expression = Expression("12 18 2")
            expression.eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("Too many numbers or variables", err)
    }

    @Test
    fun testInvalidExpressions3() {
        var err = ""
        try {
            val expression = Expression("12 + * 18")
            expression.eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("Unknown unary operator '*' at position 6", err)
    }

    @Test
    fun testInvalidExpressions4() {
        var err = ""
        try {
            val expression = Expression("")
            expression.eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("Empty expression", err)
    }

    @Test
    fun testBrackets() {
        assertEquals("3", Expression("(1+2)").eval()!!.toPlainString())
        assertEquals("3", Expression("((1+2))").eval()!!.toPlainString())
        assertEquals("3", Expression("(((1+2)))").eval()!!.toPlainString())
        assertEquals("9", Expression("(1+2)*(1+2)").eval()!!.toPlainString())
        assertEquals("10", Expression("(1+2)*(1+2)+1").eval()!!.toPlainString())
        assertEquals("12", Expression("(1+2)*((1+2)+1)").eval()!!.toPlainString())
    }

    @Test
    fun testUnknow1() {
        Assertions.assertThrows(RuntimeException::class.java
        ) { assertEquals("", Expression("7#9").eval()!!.toPlainString()) }
    }

    @Test
    fun testUnknow2() {
        Assertions.assertThrows(RuntimeException::class.java
        ) { assertEquals("", Expression("123.6*-9.8-7#9").eval()!!.toPlainString()) }

    }

    @Test
    fun testSimple() {
        assertEquals("3", Expression("1+2").eval()!!.toPlainString())
        assertEquals("2", Expression("4/2").eval()!!.toPlainString())
        assertEquals("5", Expression("3+4/2").eval()!!.toPlainString())
        assertEquals("3.5", Expression("(3+4)/2").eval()!!.toPlainString())
        assertEquals("7.98", Expression("4.2*1.9").eval()!!.toPlainString())
        assertEquals("2", Expression("8%3").eval()!!.toPlainString())
        assertEquals("0", Expression("8%2").eval()!!.toPlainString())
        assertEquals("0.2", Expression("2*.1").eval()!!.toPlainString())
    }

    @Test
    @Throws(Exception::class)
    fun testUnaryMinus() {
        assertEquals("-3", Expression("-3").eval()!!.toPlainString())
        assertEquals("-2", Expression("-SQRT(4)").eval()!!.toPlainString())
        assertEquals("-12", Expression("-(5*3+(+10-13))").eval()!!.toPlainString())
        assertEquals("-2.75", Expression("-2+3/4*-1").eval()!!.toPlainString())
        assertEquals("9", Expression("-3^2").eval()!!.toPlainString())
        assertEquals("0.5", Expression("4^-0.5").eval()!!.toPlainString())
        assertEquals("-1.25", Expression("-2+3/4").eval()!!.toPlainString())
        assertEquals("-1", Expression("-(3+-4*-1/-2)").eval()!!.toPlainString())
        assertEquals("1.8", Expression("2+-.2").eval()!!.toPlainString())
    }

    @Test
    @Throws(Exception::class)
    fun testUnaryPlus() {
        assertEquals("3", Expression("+3").eval()!!.toPlainString())
        assertEquals("4", Expression("+(3-1+2)").eval()!!.toPlainString())
        assertEquals("4", Expression("+(3-(+1)+2)").eval()!!.toPlainString())
        assertEquals("9", Expression("+3^2").eval()!!.toPlainString())
    }

    @Test
    fun testPow() {
        assertEquals("16", Expression("2^4").eval()!!.toPlainString())
        assertEquals("256", Expression("2^8").eval()!!.toPlainString())
        assertEquals("9", Expression("3^2").eval()!!.toPlainString())
        assertEquals("6.25", Expression("2.5^2").eval()!!.toPlainString())
        assertEquals("28.34045", Expression("2.6^3.5").eval()!!.toPlainString())
    }

    @Test
    fun testSqrt() {
        assertEquals("4", Expression("SQRT(16)").eval()!!.toPlainString())
        assertEquals("1.41421356237", Expression("SQRT(2)").setPrecision(11).eval()!!.toPlainString())
        assertEquals("2.42187501742", Expression("SQRT(5.8654786)").setPrecision(11).eval()!!.toPlainString())
        assertEquals("1.41421356237309504880168872420969807856967187537694807317667973799073247846210703885038753432764157273501384623091229702492483606", Expression("SQRT(2)").setPrecision(128).eval()!!.toPlainString())
        assertEquals("2.236068", Expression("SQRT(5)").eval()!!.toPlainString())
        assertEquals("99.3730346", Expression("SQRT(9875)").eval()!!.toPlainString())
        assertEquals("2.3558438", Expression("SQRT(5.55)").eval()!!.toPlainString())
        assertEquals("0", Expression("SQRT(0)").eval()!!.toPlainString())
        assertEquals("1.4142135623730950488E+154", Expression("SQRT(2E308)").eval()!!.toString())
    }
    @Test
    fun testRootN() {
        assertEquals("2", Expression("ROOTN(8,3)").eval()!!.toPlainString())
        assertEquals("2.0800838231", Expression("ROOTN(9,3)").setPrecision(10).eval()!!.toPlainString())
        assertEquals("2.0800838", Expression("ROOTN(9,3)").eval()!!.toPlainString())
        assertEquals("3.8729833", Expression("ROOTN(225,4)").eval()!!.toPlainString())
        assertEquals("3.8729833462074", Expression("ROOTN(225,4)").setPrecision(13).eval()!!.toPlainString())
        assertEquals("81", Expression("ROOTN(9,.5)").eval()!!.toPlainString())
        var err = ""
        try {
            val expression = Expression("ROOTN(-9,3)").eval()!!

        } catch (e: ArithmeticException) {
            err = e.message!!
        }

        assertEquals("First argument for ROOTN(X,Y) must not be negative", err)

    }

    @Test
    fun testFunctions() {
        assertNotSame("1.5", Expression("Random()").eval()!!.toPlainString())
        assertEquals("0.400349", Expression("SIN(23.6)").eval()!!.toPlainString())
        assertEquals("8", Expression("MAX(-7,8)").eval()!!.toPlainString())
        assertEquals("5", Expression("MAX(3,max(4,5))").eval()!!.toPlainString())
        assertEquals("9.6", Expression("MAX(3,max(MAX(9.6,-4.2),Min(5,9)))").eval()!!.toPlainString())
        assertEquals("2.302585", Expression("LOG(10)").eval()!!.toPlainString())
    }

    @Test
    fun testExpectedParameterNumbers() {
        var err = ""
        try {
            val expression = Expression("Random(1)")
            expression.eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("Function Random expected 0 parameters, got 1", err)

        try {
            val expression = Expression("SIN(1, 6)")
            expression.eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("Function SIN expected 1 parameters, got 2", err)
    }

    @Test
    fun testVariableParameterNumbers() {
        var err = ""
        try {
            val expression = Expression("min()")
            expression.eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("MIN requires at least one parameter", err)

        assertEquals("1", Expression("min(1)").eval()!!.toPlainString())
        assertEquals("1", Expression("min(1, 2)").eval()!!.toPlainString())
        assertEquals("1", Expression("min(1, 2, 3)").eval()!!.toPlainString())
        assertEquals("3", Expression("max(3, 2, 1)").eval()!!.toPlainString())
        assertEquals("9", Expression("max(3, 2, 1, 4, 5, 6, 7, 8, 9, 0)").eval()!!.toPlainString())
    }

    @Test
    fun testOrphanedOperators() {
        var err = ""
        try {
            Expression("/").eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("Unknown unary operator '/' at position 1", err)

        err = ""
        try {
            Expression("3/").eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("Missing parameter(s) for operator /", err)

        err = ""
        try {
            Expression("/3").eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("Unknown unary operator '/' at position 1", err)

        err = ""
        try {
            Expression("SIN(MAX(23,45,12))/").eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("Missing parameter(s) for operator /", err)

    }


    @Test
    @Throws(Exception::class)
    fun closeParenAtStartCausesExpressionException() {
        Assertions.assertThrows(ExpressionException::class.java
        ) {
            Expression("(").eval()

        }
    }
    @Test
    fun testOrphanedOperatorsInFunctionParameters() {
        var err = ""
        try {
            Expression("min(/)").eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("Unknown unary operator '/' at position 5", err)

        err = ""
        try {
            Expression("min(3/)").eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("Missing parameter(s) for operator / at character position 5", err)

        err = ""
        try {
            Expression("min(/3)").eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("Unknown unary operator '/' at position 5", err)

        err = ""
        try {
            Expression("SIN(MAX(23,45,12,23.6/))").eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("Missing parameter(s) for operator / at character position 21", err)

        err = ""
        try {
            Expression("SIN(MAX(23,45,12/,23.6))").eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("Missing parameter(s) for operator / at character position 16", err)

        err = ""
        try {
            Expression("SIN(MAX(23,45,>=12,23.6))").eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("Unknown unary operator '>=' at position 15", err)

        err = ""
        try {
            Expression("SIN(MAX(>=23,45,12,23.6))").eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("Unknown unary operator '>=' at position 9", err)
    }

    @Test
    fun testExtremeFunctionNesting() {
        assertNotSame("1.5", Expression("Random()").eval()!!.toPlainString())
        assertEquals("0.0002791281", Expression("SIN(SIN(COS(23.6)))").eval()!!.toPlainString())
        assertEquals("-4", Expression("MIN(0, SIN(SIN(COS(23.6))), 0-MAX(3,4,MAX(0,SIN(1))), 10)").eval()!!.toPlainString())
    }

    @Test
    fun testTrigonometry() {
        assertEquals("0.5", Expression("SIN(30)").eval()!!.toPlainString())
        assertEquals("0.8660254", Expression("cos(30)").eval()!!.toPlainString())
        assertEquals("0.5773503", Expression("TAN(30)").eval()!!.toPlainString())
        assertEquals("5343237000000", Expression("SINH(30)").eval()!!.toPlainString())
        assertEquals("5343237000000", Expression("COSH(30)").eval()!!.toPlainString())
        assertEquals("1", Expression("TANH(30)").eval()!!.toPlainString())
        assertEquals("0.5235988", Expression("RAD(30)").eval()!!.toPlainString())
        assertEquals("1718.873", Expression("DEG(30)").eval()!!.toPlainString())
        assertEquals("30", Expression("atan(0.5773503)").eval()!!.toPlainString())
        assertEquals("30", Expression("atan2(0.5773503, 1)").eval()!!.toPlainString())
        assertEquals("33.69007", Expression("atan2(2, 3)").eval()!!.toPlainString())
        assertEquals("146.3099", Expression("atan2(2, -3)").eval()!!.toPlainString())
        assertEquals("-146.3099", Expression("atan2(-2, -3)").eval()!!.toPlainString())
        assertEquals("-33.69007", Expression("atan2(-2, 3)").eval()!!.toPlainString())
        assertEquals("1.154701", Expression("SEC(30)").eval()!!.toPlainString())
        assertEquals("1.414214", Expression("SEC(45)").eval()!!.toPlainString())
        assertEquals("2", Expression("SEC(60)").eval()!!.toPlainString())
        assertEquals("3.863703", Expression("SEC(75)").eval()!!.toPlainString())
        assertEquals("2", Expression("CSC(30)").eval()!!.toPlainString())
        assertEquals("1.414214", Expression("CSC(45)").eval()!!.toPlainString())
        assertEquals("1.154701", Expression("CSC(60)").eval()!!.toPlainString())
        assertEquals("1.035276", Expression("CSC(75)").eval()!!.toPlainString())
        assertEquals("0.0000000000001871525", Expression("SECH(30)").eval()!!.toPlainString())
        assertEquals("0.00000000000000000005725037", Expression("SECH(45)").eval()!!.toPlainString())
        assertEquals("0.00000000000000000000000001751302", Expression("SECH(60)").eval()!!.toPlainString())
        assertEquals("0.000000000000000000000000000000005357274",
                Expression("SECH(75)").eval()!!.toPlainString())
        assertEquals("0.0000000000001871525", Expression("CSCH(30)").eval()!!.toPlainString())
        assertEquals("0.00000000000000000005725037", Expression("CSCH(45)").eval()!!.toPlainString())
        assertEquals("0.00000000000000000000000001751302", Expression("CSCH(60)").eval()!!.toPlainString())
        assertEquals("0.000000000000000000000000000000005357274",
                Expression("CSCH(75)").eval()!!.toPlainString())
        assertEquals("1.732051", Expression("COT(30)").eval()!!.toPlainString())
        assertEquals("1", Expression("COT(45)").eval()!!.toPlainString())
        assertEquals("0.5773503", Expression("COT(60)").eval()!!.toPlainString())
        assertEquals("0.2679492", Expression("COT(75)").eval()!!.toPlainString())
        assertEquals("1.909152", Expression("ACOT(30)").eval()!!.toPlainString())
        assertEquals("1.27303", Expression("ACOT(45)").eval()!!.toPlainString())
        assertEquals("0.9548413", Expression("ACOT(60)").eval()!!.toPlainString())
        assertEquals("0.7638985", Expression("ACOT(75)").eval()!!.toPlainString())
        assertEquals("1", Expression("COTH(30)").eval()!!.toPlainString())
        assertEquals("1.199538", Expression("COTH(1.2)").eval()!!.toPlainString())
        assertEquals("1.016596", Expression("COTH(2.4)").eval()!!.toPlainString())
        assertEquals("4.094622", Expression("ASINH(30)").eval()!!.toPlainString())
        assertEquals("4.499933", Expression("ASINH(45)").eval()!!.toPlainString())
        assertEquals("4.787561", Expression("ASINH(60)").eval()!!.toPlainString())
        assertEquals("5.01068", Expression("ASINH(75)").eval()!!.toPlainString())
        assertEquals("0", Expression("ACOSH(1)").eval()!!.toPlainString())
        assertEquals("4.094067", Expression("ACOSH(30)").eval()!!.toPlainString())
        assertEquals("4.499686", Expression("ACOSH(45)").eval()!!.toPlainString())
        assertEquals("4.787422", Expression("ACOSH(60)").eval()!!.toPlainString())
        assertEquals("5.010591", Expression("ACOSH(75)").eval()!!.toPlainString())
        assertEquals("0", Expression("ATANH(0)").eval()!!.toPlainString())
        assertEquals("0.5493061", Expression("ATANH(0.5)").eval()!!.toPlainString())
        assertEquals("-0.5493061", Expression("ATANH(-0.5)").eval()!!.toPlainString())
    }

    @Test
    fun testMinMaxAbs() {
        assertEquals("3.78787", Expression("MAX(3.78787,3.78786)").eval()!!.toPlainString())
        assertEquals("3.78787", Expression("max(3.78786,3.78787)").eval()!!.toPlainString())
        assertEquals("3.78786", Expression("MIN(3.78787,3.78786)").eval()!!.toPlainString())
        assertEquals("3.78786", Expression("Min(3.78786,3.78787)").eval()!!.toPlainString())
        assertEquals("2.123", Expression("aBs(-2.123)").eval()!!.toPlainString())
        assertEquals("2.123", Expression("abs(2.123)").eval()!!.toPlainString())
    }

    @Test
    fun testRounding() {
        assertEquals("3.8", Expression("round(3.78787,1)").eval()!!.toPlainString())
        assertEquals("3.788", Expression("round(3.78787,3)").eval()!!.toPlainString())
        assertEquals("3.735", Expression("round(3.7345,3)").eval()!!.toPlainString())
        assertEquals("-3.735", Expression("round(-3.7345,3)").eval()!!.toPlainString())
        assertEquals("-3.79", Expression("round(-3.78787,2)").eval()!!.toPlainString())
        assertEquals("123.79", Expression("round(123.78787,2)").eval()!!.toPlainString())
        assertEquals("3", Expression("floor(3.78787)").eval()!!.toPlainString())
        assertEquals("4", Expression("ceiling(3.78787)").eval()!!.toPlainString())
        assertEquals("-3", Expression("floor(-2.1)").eval()!!.toPlainString())
        assertEquals("-2", Expression("ceiling(-2.1)").eval()!!.toPlainString())
    }

    @Test
    fun testMathContext() {
        var e: Expression?
        e = Expression("2.5/3").setPrecision(2)
        assertEquals("0.83", e.eval()!!.toPlainString())

        e = Expression("2.5/3").setPrecision(3)
        assertEquals("0.833", e.eval()!!.toPlainString())

        e = Expression("2.5/3").setPrecision(8)
        assertEquals("0.83333333", e.eval()!!.toPlainString())

        e = Expression("2.5/3").setRoundingMode(RoundingMode.DOWN)
        assertEquals("0.8333333", e.eval()!!.toPlainString())

        e = Expression("2.5/3").setRoundingMode(RoundingMode.UP)
        assertEquals("0.8333334", e.eval()!!.toPlainString())
    }

    @Test
    @Throws(Exception::class)
    fun unknownFunctionsFailGracefully() {
        var err = ""
        try {
            Expression("unk(1,2,3)").eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("Unknown function 'unk' at position 1", err)
    }

    @Test
    @Throws(Exception::class)
    fun unknownOperatorsFailGracefully() {
        var err = ""
        try {
            Expression("a |*| b").eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("Unknown operator '|*|' at position 3", err)
    }

    @Test
    fun testNull() {
        val e = Expression("null")
        assertEquals(null, e.eval())
    }

    @Test
    fun testCalculationWithNull() {
        var err = ""
        try {
            Expression("null+1").eval()
        } catch (e: ArithmeticException) {
           err = e.message!!
        }

        assertEquals("First operand may not be null", err)

        err = ""
        try {
            Expression("1 + NULL").eval()
        } catch (e: ArithmeticException) {
            err = e.message!!
        }

        assertEquals("Second operand may not be null", err)

        err = ""
        try {
            Expression("round(NULL, 1)").eval()
        } catch (e: ArithmeticException) {
            err = e.message!!
        }

        assertEquals("First operand may not be null", err)

        err = ""
        try {
            Expression("round(1, NulL)").eval()
        } catch (e: ArithmeticException) {
            err = e.message!!
        }

        assertEquals("Second operand may not be null", err)
    }

    @Test
    @Throws(Exception::class)
    fun canEvalHexExpression() {
        val result = Expression("0xcafe").eval()
        assertEquals("51966", result!!.toPlainString())
    }

    @Test
    @Throws(Exception::class)
    fun hexExpressionCanUseUpperCaseCharacters() {
        val result = Expression("0XCAFE").eval()
        assertEquals("51966", result!!.toPlainString())
    }

    @Test
    @Throws(Exception::class)
    fun longHexExpressionWorks() {
        val result = Expression("0xcafebabe", MathContext.DECIMAL128).eval()
        assertEquals("3405691582", result!!.toPlainString())
    }

    @Test
    @Throws(Exception::class)
    fun hexExpressionDoesNotAllowNonHexCharacters() {
        Assertions.assertThrows(ExpressionException::class.java
        ) { val result = Expression("0xbaby").eval() }
    }

    @Test
    @Throws(Exception::class)
    fun throwsExceptionIfDoesNotContainHexDigits() {
        Assertions.assertThrows(NumberFormatException::class.java
        ) { val result = Expression("0x").eval() }
    }

    @Test
    @Throws(Exception::class)
    fun hexExpressionsEvaluatedAsExpected() {
        val result = Expression("0xcafe + 0xbabe").eval()
        assertEquals("99772", result!!.toPlainString())
    }

    @Test
    fun testResultZeroStripping() {
        val expression = Expression("200.40000 / 2")
        assertEquals("100.2", expression.eval()!!.toPlainString())
        assertEquals("100.2000", expression.eval(false)!!.toPlainString())
    }

    @Test
    fun testImplicitMultiplication() {
        var expression = Expression("22(3+1)")
        assertEquals("88", expression.eval()!!.toPlainString())

        expression = Expression("(a+b)(a-b)")
        assertEquals("-3", expression.where("a", "1").where("b", "2").eval()!!.toPlainString())

        expression = Expression("0xA(a+b)")
        assertEquals("30", expression.where("a", "1").where("b", "2").eval()!!.toPlainString())
    }

    @Test
    fun testNoLeadingZero() {
        var e = Expression("0.1 + .1")
        assertEquals("0.2", e.eval()!!.toPlainString())

        e = Expression(".2*.3")
        assertEquals("0.06", e.eval()!!.toPlainString())

        e = Expression(".2*.3+.1")
        assertEquals("0.16", e.eval()!!.toPlainString())
    }

    @Test
    fun testUnexpectedComma() {
        var err = ""
        try {
            val expression = Expression("2+3,8")
            expression.eval()
        } catch (e: ExpressionException) {
            err = e.message!!
        }

        assertEquals("Unexpected comma at character position 3", err)
    }
}

