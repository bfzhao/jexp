package org.my;

import static org.my.Exp4jException.*;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.lang.Math.*;
import static org.junit.Assert.*;

public class ExpressionBuilderTest {
    private final IExpressionContext context = Entry.buildContext();

    @Test
    public void testExpressionBuilder1() {
        double result = Entry.buildExpressionBuilder("2+1")
                .build()
                .evaluate(context).asDouble();
        assertEquals(3d, result, 0d);
    }

    @Test
    public void testExpressionBuilder2() {
        IExpressionContext context = Entry.buildContext();
        context.updateVariable("x", Math.PI);
        double result = Entry.buildExpressionBuilder("cos(x)")
                .build()
                .evaluate(context).asDouble();
        double expected = cos(Math.PI);
        assertEquals(expected, result, 0d);
    }

    @Test
    public void testExpressionBuilder3() {
        IExpressionContext context = Entry.buildContext();
        context.updateVariable("x", Math.PI);
        double x = Math.PI;
        double result = Entry.buildExpressionBuilder("sin(x)-log(3*x/4)")
                .build()
                .evaluate(context).asDouble();

        double expected = sin(x) - log(3 * x / 4);
        assertEquals(expected, result, 0d);
    }

    @Test
    public void testExpressionBuilder10() {
        double result = Entry.buildExpressionBuilder("1e1")
                .build()
                .evaluate(context).asDouble();
        assertEquals(10d, result, 0d);
    }

    @Test
    public void testExpressionBuilder11() {
        double result = Entry.buildExpressionBuilder("1.11e-1")
                .build()
                .evaluate(context).asDouble();
        assertEquals(0.111d, result, 0d);
    }

    @Test
    public void testExpressionBuilder12() {
        double result = Entry.buildExpressionBuilder("1.11e+1")
                .build()
                .evaluate(context).asDouble();
        assertEquals(11.1d, result, 0d);
    }

    @Test
    public void testExpressionBuilder13() {
        double result = Entry.buildExpressionBuilder("-3^2")
                .build()
                .evaluate(context).asDouble();
        assertEquals(-9d, result, 0d);
    }

    @Test
    public void testExpressionBuilder14() {
        double result = Entry.buildExpressionBuilder("(-3)^2")
                .build()
                .evaluate(context).asDouble();
        assertEquals(9d, result, 0d);
    }

    @Test
    public void testExpressionBuilder15() {
        double v = Entry.buildExpressionBuilder("-3/0").build().evaluate(context).asDouble();
        assertEquals(Double.NEGATIVE_INFINITY, v, 0.0);
    }

    @Test
    public void testExpressionBuilder16() {
        context.updateVariable("x", 1d);
        context.updateVariable("y", 2d);
        Entry.buildExpressionBuilder("log(x) - y * (sqrt(x^cos(y)))")
                .build()
                .evaluate(context).asDouble();
    }

    /* legacy tests from earlier my versions */
    @Test
    public void testModulo1() {
        double result = Entry.buildExpressionBuilder("33%(20/2)%2")
                .build().evaluate(context).asDouble();
        assertEquals(1d, result, 0.0);
    }

    @Test
    public void testExpressionBuilder01() {
        context.updateVariable("x", 1d);
        context.updateVariable("y", 2d);
        IExpression e = Entry.buildExpressionBuilder("7*x + 3*y").build();
        double result = e.evaluate(context).asDouble();
        assertEquals(13d, result, 0.0);
    }

    @Test
    public void testExpressionBuilder03() {
        double varX = 1.3d;
        double varY = 4.22d;
        context.updateVariable("x", varX);
        context.updateVariable("y", varY);
        IExpression e = Entry.buildExpressionBuilder("7*x + 3*y - log(y/x*12)^y")
                .build();
        double result = e.evaluate(context).asDouble();
        assertEquals(result, 7 * varX + 3 * varY - pow(log(varY / varX * 12), varY), 0.0);
    }

    @Test
    public void testExpressionBuilder04() {
        double varX = 1.3d;
        double varY = 4.22d;
        context.updateVariable("x", varX);
        context.updateVariable("y", varY);
        IExpression e =
                Entry.buildExpressionBuilder("7*x + 3*y - log(y/x*12)^y")
                        .build();
        double result = e.evaluate(context).asDouble();
        assertEquals(result, 7 * varX + 3 * varY - pow(log(varY / varX * 12), varY), 0.0);
        varX = 1.79854d;
        varY = 9281.123d;
        context.updateVariable("x", varX);
        context.updateVariable("y", varY);
        result = e.evaluate(context).asDouble();
        assertEquals(result, 7 * varX + 3 * varY - pow(log(varY / varX * 12), varY), 0.0);
    }

    @Test
    public void testExpressionBuilder05() {
        double varX = 1.3d;
        double varY = 4.22d;
        context.updateVariable("x", varX);
        context.updateVariable("y", varY);
        IExpression e = Entry.buildExpressionBuilder("3*y")
                .build();
        double result = e.evaluate(context).asDouble();
        assertEquals(result, 3 * varY, 0.0);
    }

    @Test
    public void testExpressionBuilder06() {
        double varX = 1.3d;
        double varY = 4.22d;
        double varZ = 4.22d;
        context.updateVariable("x", varX);
        context.updateVariable("y", varY);
        context.updateVariable("z", varZ);
        IExpression e = Entry.buildExpressionBuilder("x * y * z")
                .build();
        double result = e.evaluate(context).asDouble();
        assertEquals(result, varX * varY * varZ, 0.0);
    }

    @Test
    public void testExpressionBuilder07() {
        double varX = 1.3d;
        context.updateVariable("x", varX);
        IExpression e = Entry.buildExpressionBuilder("log(sin(x))")
                .build();
        double result = e.evaluate(context).asDouble();
        assertEquals(result, log(sin(varX)), 0.0);
    }

    @Test(expected = EvaluationException.class)
    public void testMissingVar() {
        double varY = 4.22d;
        context.updateVariable("y", varY);
        IExpression e = Entry.buildExpressionBuilder("3*y*z")
                .build();
        e.evaluate(context).asDouble();
    }

    @Test
    public void testUnaryMinusPowerPrecedence() {
        IExpression e = Entry.buildExpressionBuilder("-1^2")
                .build();
        assertEquals(-1d, e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testUnaryMinus() {
        IExpression e = Entry.buildExpressionBuilder("-1")
                .build();
        assertEquals(-1d, e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression1() {
        String expr;
        double expected;
        expr = "2 + 4";
        expected = 6d;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression10() {
        String expr;
        double expected;
        expr = "1 * 1.5 + 1";
        expected = 1 * 1.5 + 1;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression11() {
        double x = 1d;
        double y = 2d;
        context.updateVariable("x", x);
        context.updateVariable("y", y);
        String expr = "log(x) ^ sin(y)";
        double expected = Math.pow(Math.log(x), Math.sin(y));
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression12() {
        String expr = "log(2.5333333333)^(0-1)";
        double expected = Math.pow(Math.log(2.5333333333d), -1);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression13() {
        String expr = "2.5333333333^(0-1)";
        double expected = Math.pow(2.5333333333d, -1);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression14() {
        String expr = "2 * 17.41 + (12*2)^(0-1)";
        double expected = 2 * 17.41d + Math.pow((12 * 2), -1);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression15() {
        String expr = "2.5333333333 * 17.41 + (12*2)^log(2.764)";
        double expected = 2.5333333333d * 17.41d + Math.pow((12 * 2), Math.log(2.764d));
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression16() {
        String expr = "2.5333333333/2 * 17.41 + (12*2)^(log(2.764) - sin(5.6664))";
        double expected = 2.5333333333d / 2 * 17.41d + Math.pow((12 * 2), Math.log(2.764d) - Math.sin(5.6664d));
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression17() {
        String expr = "x^2 - 2 * y";
        double x = Math.E;
        double y = Math.PI;
        context.updateVariable("x", x);
        context.updateVariable("y", y);
        double expected = x * x - 2 * y;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression18() {
        String expr = "-3";
        double expected = -3;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression19() {
        String expr = "-3 * -24.23";
        double expected = -3 * -24.23d;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression2() {
        String expr;
        double expected;
        expr = "2+3*4-12";
        expected = 2 + 3 * 4 - 12;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression20() {
        String expr = "-2 * 24/log(2) -2";
        double expected = -2 * 24 / Math.log(2) - 2;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression21() {
        String expr = "-2 *33.34/log(x)^-2 + 14 *6";
        double x = 1.334d;
        context.updateVariable("x", x);
        double expected = -2 * 33.34 / Math.pow(Math.log(x), -2) + 14 * 6;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpressionPower() {
        String expr = "2^-2";
        double expected = Math.pow(2, -2);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpressionMultiplication() {
        String expr = "2*-2";
        double expected = -4d;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression22() {
        String expr = "-2 *33.34/log(x)^-2 + 14 *6";
        double x = 1.334d;
        context.updateVariable("x", x);
        double expected = -2 * 33.34 / Math.pow(Math.log(x), -2) + 14 * 6;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression23() {
        String expr = "-2 *33.34/(log(foo)^-2 + 14 *6) - sin(foo)";
        double x = 1.334d;
        double expected = -2 * 33.34 / (Math.pow(Math.log(x), -2) + 14 * 6) - Math.sin(x);
        context.updateVariable("foo", x);

        IExpression e = Entry.buildExpressionBuilder(expr).build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression24() {
        String expr = "3+4-log(23.2)^(2-1) * -1";
        double expected = 3 + 4 - Math.pow(Math.log(23.2), (2 - 1)) * -1;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression25() {
        String expr = "+3+4-+log(23.2)^(2-1) * + 1";
        double expected = 3 + 4 - Math.log(23.2d);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression26() {
        String expr = "14 + -(1 / 2.22^3)";
        double expected = 14 + -(1d / Math.pow(2.22d, 3d));
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression27() {
        String expr = "12^-+-+-+-+-+-+---2";
        double expected = Math.pow(12, -2);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression28() {
        String expr = "12^-+-+-+-+-+-+---2 * (-14) / 2 ^ -log(2.22323) ";
        double expected = Math.pow(12, -2) * -14 / Math.pow(2, -Math.log(2.22323));
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression29() {
        String expr = "24.3343 % 3";
        double expected = 24.3343 % 3;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testVarName1() {
        String expr = "12.23 * foo_bar";
        context.updateVariable("foo_bar", 1d);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(12.23, e.evaluate(context).asDouble(), 0.0);
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidNumberOfArguments1() {
        String expr = "log(2,2)";
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        e.evaluate(context).asDouble();
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidNumberOfArguments2() {
        String expr = "avg(2,2)";
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        e.evaluate(context).asDouble();
    }

    @Test
    public void testExpression3() {
        String expr;
        double expected;
        expr = "2+4*5";
        expected = 2 + 4 * 5;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression30() {
        String expr = "24.3343 % 3 * 20 ^ -(2.334 % log(2.0 / 14))";
        double expected = 24.3343d % 3 * Math.pow(20, -(2.334 % Math.log(2d / 14d)));
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression31() {
        String expr = "-2 *33.34/log(y_x)^-2 + 14 *6";
        double x = 1.334d;
        double expected = -2 * 33.34 / Math.pow(Math.log(x), -2) + 14 * 6;
        context.updateVariable("y_x", x);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression32() {
        String expr = "-2 *33.34/log(y_2x)^-2 + 14 *6";
        double x = 1.334d;
        double expected = -2 * 33.34 / Math.pow(Math.log(x), -2) + 14 * 6;
        context.updateVariable("y_2x", x);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression33() {
        String expr = "-2 *33.34/log(_y)^-2 + 14 *6";
        double x = 1.334d;
        double expected = -2 * 33.34 / Math.pow(Math.log(x), -2) + 14 * 6;
        context.updateVariable("_y", x);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression34() {
        String expr = "-2 + + (+4) +(4)";
        double expected = -2 + 4 + 4;
        IExpression e = Entry.buildExpressionBuilder(expr).build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression40() {
        String expr = "1e1";
        double expected = 10d;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression41() {
        String expr = "1e-1";
        double expected = 0.1d;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    /*
     * Added tests for expressions with scientific notation see http://jira.congrace.de/jira/browse/EXP-17
     */
    @Test
    public void testExpression42() {
        String expr = "7.2973525698e-3";
        double expected = 7.2973525698e-3d;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression43() {
        String expr = "6.02214E23";
        double expected = 6.02214e23d;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        double result = e.evaluate(context).asDouble();
        assertEquals(expected, result, 0.0);
    }

    @Test
    public void testExpression44() {
        String expr = "6.02214E23";
        double expected = 6.02214e23d;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test(expected = ParseException.class)
    public void testExpression45() {
        String expr = "6.02214E2E3";
        Entry.buildExpressionBuilder(expr).build();
    }

    @Test(expected = ParseException.class)
    public void testExpression46() {
        String expr = "6.02214e2E3";
        Entry.buildExpressionBuilder(expr).build();
    }

    // tests for EXP-20: No exception is thrown for unmatched parenthesis in
    // build
    // Thanks go out to maheshkurmi for reporting
    @Test(expected = ParseException.class)
    public void testExpression48() {
        String expr = "(1*2";
        IExpression e = Entry.buildExpressionBuilder(expr).build();
        e.evaluate(context).asDouble();
    }

    @Test(expected = ParseException.class)
    public void testExpression49() {
        String expr = "{1*2";
        IExpression e = Entry.buildExpressionBuilder(expr).build();
        e.evaluate(context).asDouble();
    }

    @Test(expected = ParseException.class)
    public void testExpression50() {
        String expr = "[1*2";
        IExpression e = Entry.buildExpressionBuilder(expr).build();
        e.evaluate(context).asDouble();
    }

    @Test(expected = ParseException.class)
    public void testExpression51() {
        String expr = "(1*{2+[3}";
        IExpression e = Entry.buildExpressionBuilder(expr).build();
        e.evaluate(context).asDouble();
    }

    @Test(expected = ParseException.class)
    public void testExpression52() {
        String expr = "(1*(2+(3";
        IExpression e = Entry.buildExpressionBuilder(expr).build();
        e.evaluate(context).asDouble();
    }

    @Test
    public void testExpression53() {
        String expr = "14 * 2*x";
        context.updateVariable("x", 1.5d);
        IExpression exp = Entry.buildExpressionBuilder(expr).build();
//        assertTrue(exp.validate(context).isValid());
        assertEquals(14d * 2d * 1.5d, exp.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression54() {
        String expr = "2 * ((-(x)))";
        context.updateVariable("x", 1.5d);
        IExpression e = Entry.buildExpressionBuilder(expr).build();
        assertEquals(-3d, e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression55() {
        String expr = "2 * sin(x)";
        context.updateVariable("x", 2d);
        IExpression e = Entry.buildExpressionBuilder(expr).build();
        assertEquals(sin(2d) * 2, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression56() {
        String expr = "2 * sin(3*x)";
        context.updateVariable("x", 2d);
        IExpression e = Entry.buildExpressionBuilder(expr).build();
        assertEquals(sin(6d) * 2d, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testDocumentationExample1() {
        context.updateVariable("x", 2.3);
        context.updateVariable("y", 3.14);
        IExpression e = Entry.buildExpressionBuilder("3 * sin(y) - 2 / (x - 2)").build();
        double result = e.evaluate(context).asDouble();
        double expected = 3 * Math.sin(3.14d) - 2d / (2.3d - 2d);
        assertEquals(expected, result, 0d);
    }

    @Test
    public void testDocumentationExample2() throws Exception {
        ExecutorService exec = Executors.newFixedThreadPool(1);
        context.updateVariable("x", 2.3);
        context.updateVariable("y", 3.14);
        IExpression e = Entry.buildExpressionBuilder("3*log(y)/(x+1)").build();
        Future<Value> result = e.evaluateAsync(exec, context);
        double expected = 3 * Math.log(3.14d) / (3.3);
        assertEquals(expected, result.get().asDouble(), 0.00000001d);
    }

    @Test
    public void testDocumentationExample3() {
        context.updateVariable("x", 0.5d);
        context.updateVariable("y", 0.25d);
        double result = Entry.buildExpressionBuilder("2*cos(x*y)")
                .build()
                .evaluate(context).asDouble();
        assertEquals(2d * Math.cos(0.5d * 0.25d), result, 0d);
    }

    @Test
    public void testDocumentationExample5() {
        String expr = "7.2973525698e-3";
        double expected = Double.parseDouble(expr);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression57() {
        String expr = "1 / 0";
        IExpression e = Entry.buildExpressionBuilder(expr).build();
        assertEquals(Double.POSITIVE_INFINITY, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression58() {
        String expr = "17 * sqrt(-1) * 12";
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertTrue(Double.isNaN(e.evaluate(context).asDouble()));
    }

    @Test
    public void testExpression59() {
        assertNull(Entry.buildExpressionBuilder("").build());
    }

    @Test
    public void testExpression60() {
        assertNull(Entry.buildExpressionBuilder("   ").build());
    }

    @Test
    public void testExpression61() {
        double v = Entry.buildExpressionBuilder("14 % 0").build().evaluate(context).asDouble();
        assertEquals(Double.NaN, v, 0.0);
    }

    // https://www.objecthunter.net/jira/browse/EXP-24
    // thanks go out to Rémi for the issue report
    @Test
    public void testExpression62() {
        context.updateVariable("x", E);
        IExpression e = Entry.buildExpressionBuilder("x*1.0e5+5")
                .build();
        assertEquals(E * 1.0 * pow(10, 5) + 5, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression63() {
        IExpression e = Entry.buildExpressionBuilder("log10(5)")
                .build();
        assertEquals(Math.log10(5), e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression64() {
        IExpression e = Entry.buildExpressionBuilder("log2(5)")
                .build();
        assertEquals(Math.log(5) / Math.log(2), e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression65() {
        IExpression e = Entry.buildExpressionBuilder("2*log(e)")
                .build();

        assertEquals(2d, e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression66() {
        IExpression e = Entry.buildExpressionBuilder("log(e)*2")
                .build();

        assertEquals(2d, e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression67() {
        IExpression e = Entry.buildExpressionBuilder("2*e*sin(pi/2)")
                .build();

        assertEquals(2 * Math.E * Math.sin(Math.PI / 2d), e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression68() {
        context.updateVariable("x", Math.E);
        IExpression e = Entry.buildExpressionBuilder("2*x")
                .build();
        assertEquals(2 * Math.E, e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression69() {
        context.updateVariable("x", Math.E);
        IExpression e = Entry.buildExpressionBuilder("2*x*2")
                .build();
        assertEquals(4 * Math.E, e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression70() {
        context.updateVariable("x", Math.E);
        IExpression e = Entry.buildExpressionBuilder("2*x*x")
                .build();
        assertEquals(2 * Math.E * Math.E, e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression71() {
        context.updateVariable("x", Math.E);
        IExpression e = Entry.buildExpressionBuilder("x*2*x")
                .build();
        assertEquals(2 * Math.E * Math.E, e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression72() {
        context.updateVariable("x", Math.E);
        IExpression e = Entry.buildExpressionBuilder("2*cos(x)")
                .build();
        assertEquals(2 * Math.cos(Math.E), e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression73() {
        context.updateVariable("x", Math.E);
        IExpression e = Entry.buildExpressionBuilder("cos(x)*2")
                .build();
        assertEquals(2 * Math.cos(Math.E), e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression74() {
        context.updateVariable("x", Math.E);
        IExpression e = Entry.buildExpressionBuilder("cos(x)*(-2)")
                .build();
        assertEquals(-2d * Math.cos(Math.E), e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression75() {
        context.updateVariable("x", Math.E);
        IExpression e = Entry.buildExpressionBuilder("(-2)*cos(x)")
                .build();
        assertEquals(-2d * Math.cos(Math.E), e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression76() {
        context.updateVariable("x", Math.E);
        IExpression e = Entry.buildExpressionBuilder("(-x)*cos(x)")
                .build();
        assertEquals(-E * Math.cos(Math.E), e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression77() {
        context.updateVariable("x", Math.E);
        IExpression e = Entry.buildExpressionBuilder("(-x*x)*cos(x)")
                .build();
        assertEquals(-E * E * Math.cos(Math.E), e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression78() {
        context.updateVariable("x", Math.E);
        IExpression e = Entry.buildExpressionBuilder("(x*x)*cos(x)")
                .build();
        assertEquals(E * E * Math.cos(Math.E), e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression79() {
        context.updateVariable("x", Math.E);
        IExpression e = Entry.buildExpressionBuilder("cos(x)*(x*x)")
                .build();
        assertEquals(E * E * Math.cos(Math.E), e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression80() {
        context.updateVariable("x", Math.E);
        context.updateVariable("y", Math.sqrt(2));
        IExpression e = Entry.buildExpressionBuilder("cos(x)*(x*y)")
                .build();
        assertEquals(sqrt(2) * E * Math.cos(Math.E), e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression81() {
        context.updateVariable("x", Math.E);
        context.updateVariable("y", Math.sqrt(2));
        IExpression e = Entry.buildExpressionBuilder("cos(x*y)")
                .build();
        assertEquals(cos(sqrt(2) * E), e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression82() {
        context.updateVariable("x", Math.E);
        IExpression e = Entry.buildExpressionBuilder("cos(2*x)")
                .build();
        assertEquals(cos(2 * E), e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression83() {
        context.updateVariable("x", Math.E);
        context.updateVariable("y", Math.sqrt(2));
        IExpression e = Entry.buildExpressionBuilder("cos(x*log(x*y))")
                .build();
        assertEquals(cos(E * log(E * sqrt(2))), e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression84() {
        context.updateVariable("x_1", Math.E);
        IExpression e = Entry.buildExpressionBuilder("3*x_1")
                .build();
        assertEquals(3d * E, e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testExpression85() {
        context.updateVariable("x", 6d);
        IExpression e = Entry.buildExpressionBuilder("1.0/2*x")
                .build();
        assertEquals(3d, e.evaluate(context).asDouble(), 0d);
    }

    // thanks got out to David Sills
    @Test(expected = ParseException.class)
    public void testSpaceBetweenNumbers() {
        Entry.buildExpressionBuilder("1 1").build().evaluate();
    }

    // thanks go out to Janny for providing the tests and the bug report
    @Test
    public void testUnaryMinusInParenthesisSpace() {
        IExpressionBuilder b = Entry.buildExpressionBuilder("( -1)^2");
        double calculated = b.build().evaluate(context).asDouble();
        assertEquals(1d, calculated, 0.0);
    }

    @Test
    public void testUnaryMinusSpace() {
        IExpressionBuilder b = Entry.buildExpressionBuilder(" -1 + 2");
        double calculated = b.build().evaluate(context).asDouble();
        assertEquals(1d, calculated, 0.0);
    }

    @Test
    public void testUnaryMinusSpaces() {
        IExpressionBuilder b = Entry.buildExpressionBuilder(" -1 + + 2 +   -   1");
        double calculated = b.build().evaluate(context).asDouble();
        assertEquals(0d, calculated, 0.0);
    }

    @Test
    public void testUnaryMinusSpace1() {
        IExpressionBuilder b = Entry.buildExpressionBuilder("-1");
        double calculated = b.build().evaluate(context).asDouble();
        assertEquals(calculated, -1d, 0.0);
    }

    @Test
    public void testExpression4() {
        String expr;
        double expected;
        expr = "2+4 * 5";
        expected = 2 + 4 * 5;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression5() {
        String expr;
        double expected;
        expr = "(2+4)*5";
        expected = (2 + 4) * 5;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression6() {
        String expr;
        double expected;
        expr = "(2+4)*5 + 2.5*2";
        expected = (2 + 4) * 5 + 2.5 * 2;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression7() {
        String expr;
        double expected;
        expr = "(2+4)*5 + 10/2";
        expected = (2 + 4) * 5 + 10d / 2;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression8() {
        String expr;
        double expected;
        expr = "(2 * 3 +4)*5 + 10/2";
        expected = (2 * 3 + 4) * 5 + 10d / 2;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testExpression9() {
        String expr;
        double expected;
        expr = "(2 * 3 +4)*5 +4 + 10/2";
        expected = 59; //(2 * 3 + 4) * 5 + 4 + 10 / 2 = 59
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test(expected = EvaluationException.class)
    public void testFailUnknownFunction1() {
        String expr;
        expr = "lig(1)";
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        e.evaluate(context).asDouble();
    }

    @Test(expected = EvaluationException.class)
    public void testFailUnknownFunction2() {
        String expr;
        expr = "galength(1)";
        Entry.buildExpressionBuilder(expr).build().evaluate(context).asDouble();
    }

    @Test(expected = EvaluationException.class)
    public void testFailUnknownFunction3() {
        String expr;
        expr = "tcos(1)";
        IExpression exp = Entry.buildExpressionBuilder(expr).build();
        double result = exp.evaluate(context).asDouble();
        System.out.println(result);
    }

    @Test
    public void testFunction22() {
        String expr;
        expr = "cos(cos_1)";
        context.updateVariable("cos_1", 1d);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(e.evaluate(context).asDouble(), cos(1d), 0.0);
    }

    @Test
    public void testFunction23() {
        String expr;
        expr = "log1p(1)";
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(log1p(1d), e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testFunction24() {
        String expr;
        expr = "pow(3,3)";
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(27d, e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testPostfix1() {
        String expr;
        double expected;
        expr = "2.2232^0.1";
        expected = Math.pow(2.2232d, 0.1d);
        double actual = Entry.buildExpressionBuilder(expr)
                .build().evaluate(context).asDouble();
        assertEquals(expected, actual, 0.0);
    }

    @Test
    public void testPostfixEverything() {
        String expr;
        double expected;
        expr = "(sin(12) + log(34)) * 3.42 - cos(2.234-log(2))";
        expected = (Math.sin(12) + Math.log(34)) * 3.42 - Math.cos(2.234 - Math.log(2));
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testPostfixExponentiation1() {
        String expr;
        double expected;
        expr = "2^3";
        expected = Math.pow(2, 3);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testPostfixExponentiation2() {
        String expr;
        double expected;
        expr = "24 + 4 * 2^3";
        expected = 24 + 4 * Math.pow(2, 3);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testPostfixExponentiation3() {
        String expr;
        double expected;
        double x = 4.334d;
        expr = "24 + 4 * 2^x";
        expected = 24 + 4 * Math.pow(2, x);
        context.updateVariable("x", x);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testPostfixExponentiation4() {
        String expr;
        double expected;
        double x = 4.334d;
        expr = "(24 + 4) * 2^log(x)";
        expected = (24 + 4) * Math.pow(2, Math.log(x));
        context.updateVariable("x", x);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testPostfixFunction1() {
        String expr;
        double expected;
        expr = "log(1) * sin(0)";
        expected = Math.log(1) * Math.sin(0);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testPostfixFunction10() {
        String expr;
        double expected;
        expr = "cbrt(x)";
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected = Math.cbrt(x);
            context.updateVariable("x", x);
            assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
        }
    }

    @Test
    public void testPostfixFunction11() {
        String expr;
        double expected;
        expr = "cos(x) - (1/cbrt(x))";
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            if (x == 0d) continue;
            expected = Math.cos(x) - (1 / Math.cbrt(x));
            context.updateVariable("x", x);
            assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
        }
    }

    @Test
    public void testPostfixFunction12() {
        String expr;
        double expected;
        expr = "acos(x) * expm1(asin(x)) - exp(atan(x)) + floor(x) + cosh(x) - sinh(cbrt(x))";
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected =
                    Math.acos(x) * Math.expm1(Math.asin(x)) - Math.exp(Math.atan(x)) + Math.floor(x) + Math.cosh(x)
                            - Math.sinh(Math.cbrt(x));
            context.updateVariable("x", x);
            if (Double.isNaN(expected)) {
                assertTrue(Double.isNaN(e.evaluate(context).asDouble()));
            } else {
                assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
            }
        }
    }

    @Test
    public void testPostfixFunction13() {
        String expr;
        double expected;
        expr = "acos(x)";
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected = Math.acos(x);
            context.updateVariable("x", x);
            if (Double.isNaN(expected)) {
                assertTrue(Double.isNaN(e.evaluate(context).asDouble()));
            } else {
                assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
            }
        }
    }

    @Test
    public void testPostfixFunction14() {
        String expr;
        double expected;
        expr = " expm1(x)";
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected = Math.expm1(x);
            context.updateVariable("x", x);
            if (Double.isNaN(expected)) {
                assertTrue(Double.isNaN(e.evaluate(context).asDouble()));
            } else {
                assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
            }
        }
    }

    @Test
    public void testPostfixFunction15() {
        String expr;
        double expected;
        expr = "asin(x)";
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected = Math.asin(x);
            context.updateVariable("x", x);
            if (Double.isNaN(expected)) {
                assertTrue(Double.isNaN(e.evaluate(context).asDouble()));
            } else {
                assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
            }
        }
    }

    @Test
    public void testPostfixFunction16() {
        String expr;
        double expected;
        expr = " exp(x)";
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected = Math.exp(x);
            context.updateVariable("x", x);
            assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
        }
    }

    @Test
    public void testPostfixFunction17() {
        String expr;
        double expected;
        expr = "floor(x)";
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected = Math.floor(x);
            context.updateVariable("x", x);
            assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
        }
    }

    @Test
    public void testPostfixFunction18() {
        String expr;
        double expected;
        expr = " cosh(x)";
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected = Math.cosh(x);
            context.updateVariable("x", x);
            assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
        }
    }

    @Test
    public void testPostfixFunction19() {
        String expr;
        double expected;
        expr = "sinh(x)";
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected = Math.sinh(x);
            context.updateVariable("x", x);
            assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
        }
    }

    @Test
    public void testPostfixFunction20() {
        String expr;
        double expected;
        expr = "cbrt(x)";
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected = Math.cbrt(x);
            context.updateVariable("x", x);
            assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
        }
    }

    @Test
    public void testPostfixFunction21() {
        String expr;
        double expected;
        expr = "tanh(x)";
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected = Math.tanh(x);
            context.updateVariable("x", x);
            assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
        }
    }

    @Test
    public void testPostfixFunction2() {
        String expr;
        double expected;
        expr = "log(1)";
        expected = 0d;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testPostfixFunction3() {
        String expr;
        double expected;
        expr = "sin(0)";
        expected = 0d;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testPostfixFunction5() {
        String expr;
        double expected;
        expr = "ceil(2.3) +1";
        expected = Math.ceil(2.3) + 1;
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testPostfixFunction6() {
        String expr;
        double expected;
        double x = 1.565d;
        double y = 2.1323d;
        expr = "ceil(x) + 1 / y * abs(1.4)";
        expected = Math.ceil(x) + 1 / y * Math.abs(1.4);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        context.updateVariable("x", x);
        context.updateVariable("y", y);
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testPostfixFunction7() {
        String expr;
        double expected;
        double x = Math.E;
        expr = "tan(x)";
        expected = Math.tan(x);
        context.updateVariable("x", x);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testPostfixFunction8() {
        String expr;
        double expected;
        expr = "2^3.4223232 + tan(e)";
        expected = Math.pow(2, 3.4223232d) + Math.tan(Math.E);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testPostfixFunction9() {
        String expr;
        double expected;
        double x = Math.E;
        expr = "cbrt(x)";
        expected = Math.cbrt(x);
        context.updateVariable("x", x);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test(expected = EvaluationException.class)
    public void testPostfixInvalidVariableName() {
        String expr;
        double expected;
        double x = 4.5334332d;
        double log = Math.PI;
        expr = "x * pii";
        expected = x * log;
        context.updateVariable("x", x);
        context.updateVariable("log", log);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testPostfixParenthesis() {
        String expr;
        double expected;
        expr = "(3 + 3 * 14) * (2 * (24-17) - 14)/((34) -2)";
        expected = 0; //(3 + 3 * 14) * (2 * (24-17) - 14)/((34) -2) = 0
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testPostfixVariables() {
        String expr;
        double expected;
        double x = 4.5334332d;
        expr = "x * pi";
        expected = x * Math.PI;
        context.updateVariable("x", x);
        IExpression e = Entry.buildExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(context).asDouble(), 0.0);
    }

    @Test
    public void testUnicodeVariable1() {
        context.updateVariable("λ", E);
        IExpression e = Entry.buildExpressionBuilder("λ")
                .build();
        assertEquals(E, e.evaluate(context).asDouble(), 0d);
    }

    @Test
    public void testUnicodeVariable2() {
        context.updateVariable("ε", E);
        IExpression e = Entry.buildExpressionBuilder("log(3*ε+1)")
                .build();
        assertEquals(log(3 * E + 1), e.evaluate(context).asDouble(), 0d);
    }

    @Test(expected = EvaluationException.class)
    public void testVariableWithDot() {
        context.updateVariable("SALARY.Basic", 1.5d);
        Entry.buildExpressionBuilder("2*SALARY.Basic").build().evaluate(context);
    }

    @Test(expected = ParseException.class)
    public void testInvalidChar() {
        Entry.buildExpressionBuilder("，").build().evaluate(context);
    }

    @Test(expected = ParseException.class)
    public void testSameVariableAndBuiltinFunctionName() {
        context.updateVariable("log10", 1.5d);
        IExpression e = Entry.buildExpressionBuilder("log10(log10)").build();
        e.evaluate(context).asDouble();
    }

    @Test
    public void testSignum() {
        IExpression e = Entry.buildExpressionBuilder("signum(1)").build();
        assertEquals(1, e.evaluate(context).asLong());

        e = Entry.buildExpressionBuilder("signum(-1)").build();
        assertEquals(-1, e.evaluate(context).asLong());

        e = Entry.buildExpressionBuilder("signum(--1)").build();
        assertEquals(1, e.evaluate(context).asLong());

        e = Entry.buildExpressionBuilder("signum(+-1)").build();
        assertEquals(-1, e.evaluate(context).asLong());

        e = Entry.buildExpressionBuilder("-+1").build();
        assertEquals(-1, e.evaluate(context).asLong());

        e = Entry.buildExpressionBuilder("signum(-+1)").build();
        assertEquals(-1, e.evaluate(context).asLong());

        e = Entry.buildExpressionBuilder("signum(0)").build();
        assertEquals(0, e.evaluate(context).asLong());
    }

}
