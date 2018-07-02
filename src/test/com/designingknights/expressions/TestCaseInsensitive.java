package com.designingknights.expressions;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;


public class TestCaseInsensitive {
    @Test
    public void testVariableIsCaseInsensitive() {

        Expression expression = new Expression("a");
        expression.setVariable("A", new BigDecimal(20));
        assertEquals(expression.eval().intValue(), 20);

        expression = new Expression("a + B");
        expression.setVariable("A", new BigDecimal(10));
        expression.setVariable("b", new BigDecimal(10));
        assertEquals(expression.eval().intValue(), 20);

        expression = new Expression("a+B");
        expression.setVariable("A", "c+d");
        expression.setVariable("b", new BigDecimal(10));
        expression.setVariable("C", new BigDecimal(5));
        expression.setVariable("d", new BigDecimal(5));
        assertEquals(expression.eval().intValue(), 20);
    }

    @Test
    public void testFunctionCaseInsensitive() {

        Expression expression = new Expression("a+testsum(1,3)");
        expression.setVariable("A", new BigDecimal(1));
        expression.addFunction(expression.new Function("testSum", -1) {
            @NotNull
            @Override
            public BigDecimal eval(@NotNull List<BigDecimal> parameters) {
                BigDecimal value = null;
                for (BigDecimal d : parameters) {
                    value = value == null ? d : value.add(d);
                }
                return value;
            }

        });

        assertEquals(expression.eval(), new BigDecimal(5));

    }
}
