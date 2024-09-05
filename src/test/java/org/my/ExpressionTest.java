package org.my;

import static org.my.Exp4jException.*;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.my.Value.Type.*;
import static org.junit.Assert.*;


public class ExpressionTest {
    @Test
    public void testCotangent1() {
        IExpression e = Entry.buildExpressionBuilder("cot(1)").build();
        assertEquals(1 / Math.tan(1), e.evaluate().asDouble(), 0d);

    }

    @Test
    public void testInvalidCotangent1() {
        IExpression e = Entry.buildExpressionBuilder("cot(0)").build();
        Value v = e.evaluate();
        assertEquals(Double.POSITIVE_INFINITY, v.asDouble(), 0.0);
    }

    @Test
    public void testCsc1() {
        IExpression e = Entry.buildExpressionBuilder("csc(1)").build();
        assertEquals(1 / Math.sin(1), e.evaluate().asDouble(), 0d);
    }

    @Test
    public void testInvalidCsc1() {
        IExpression e = Entry.buildExpressionBuilder("csc(0)").build();
        Value v = e.evaluate();
        assertEquals(Double.POSITIVE_INFINITY, v.asDouble(), 0.0);
    }

    @Test
    public void testSec1() {
        IExpression e = Entry.buildExpressionBuilder("sec(1)").build();
        assertEquals(1 / Math.cos(1), e.evaluate().asDouble(), 0d);
    }

    @Test
    public void testInvalidSec1() {
        IExpression e = Entry.buildExpressionBuilder("sec(pi/2)").build();
        Value v = e.evaluate();
        assertNotNull(v);
    }

    @Test
    public void testCsch1() {
        IExpression e = Entry.buildExpressionBuilder("csch(1)").build();
        assertEquals(1 / Math.sinh(1), e.evaluate().asDouble(), 0d);
    }

    @Test
    public void testInvalidCsch() {
        IExpression e = Entry.buildExpressionBuilder("csch(0)").build();
        Value v = e.evaluate();
        assertEquals(Double.POSITIVE_INFINITY, v.asDouble(), 0.0);
    }

    @Test
    public void testSech1() {
        IExpression e = Entry.buildExpressionBuilder("sech(1)").build();
        assertEquals(1 / Math.cosh(1), e.evaluate().asDouble(), 0d);
    }

    @Test
    public void testCoth1() {
        IExpression e = Entry.buildExpressionBuilder("coth(1)").build();
        assertEquals(Math.cosh(1)/Math.sinh(1), e.evaluate().asDouble(), 0d);
    }

    @Test
    public void testlogb() {
        IExpression e = Entry.buildExpressionBuilder("logb(1,2)").build();
        assertEquals(Math.log(2)/Math.log(1), e.evaluate().asDouble(), 0d);
    }

    @Test
    public void testToRadian() {
        IExpression e = Entry.buildExpressionBuilder("toRadian(30)").build();
        assertEquals(Math.toRadians(30), e.evaluate().asDouble(), 0d);
    }

    @Test
    public void testToDegree() {
        IExpression e = Entry.buildExpressionBuilder("toDegree(pi)").build();
        assertEquals(Math.toDegrees(Math.PI), e.evaluate().asDouble(), 0d);
    }

    @Test
    public void testLongDoubleFunc() {
        assertEquals(1, Entry.buildExpressionBuilder("abs(-1)").build().evaluate().asLong());
        assertEquals(1.2d, Entry.buildExpressionBuilder("abs(-1.2)").build().evaluate().asDouble(), 0);
    }

    @Test
    public void testVectorFunc() {
        IExpression e = Entry.buildExpressionBuilder("sum([1,2,3,4])").build();
        assertEquals(10, e.evaluate().asLong());

        e = Entry.buildExpressionBuilder("sum([])").build();
        assertEquals(0, e.evaluate().asLong());

        e = Entry.buildExpressionBuilder("sum([1.2, 3, 1.1])").build();
        assertEquals(5.3, e.evaluate().asDouble(), 0.000001d);

        e = Entry.buildExpressionBuilder("count([1,2,3,4])").build();
        assertEquals(4, e.evaluate().asLong());

        e = Entry.buildExpressionBuilder("max([1,2,3,4])").build();
        assertEquals(4, e.evaluate().asLong());

        e = Entry.buildExpressionBuilder("max([1.2,3.4])").build();
        assertEquals(3.4, e.evaluate().asDouble(), 0.0000001d);

        e = Entry.buildExpressionBuilder("min([1,2,3,4])").build();
        assertEquals(1, e.evaluate().asLong());

        e = Entry.buildExpressionBuilder("min([1.2,3.4])").build();
        assertEquals(1.2, e.evaluate().asDouble(), 0.0000001d);

        e = Entry.buildExpressionBuilder("avg([1,2,3,4])").build();
        assertEquals(2.5, e.evaluate().asDouble(), 0d);

        e = Entry.buildExpressionBuilder("contains([1,2,3,4], [2,3,4])").build();
        assertTrue(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("contains([1,2,3,4], [2,2,4,4,1,1,1])").build();
        assertTrue(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("contains([1,2,3,4], [2,3,5])").build();
        assertFalse(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("contains([1,2,3,4], 7)").build();
        assertFalse(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("contains([1,2,3,4], 1)").build();
        assertTrue(e.evaluate().asBoolean());
    }

    @Test(expected = EvaluationException.class)
    public void testVectorFunc1() {
        IExpression e = Entry.buildExpressionBuilder("sum([false, true])").build();
        assertEquals(10, e.evaluate().asDouble(), 0d);
    }

    @Test(expected = EvaluationException.class)
    public void testVectorFunc2() {
        IExpression e = Entry.buildExpressionBuilder("sum([false, 12.3])").build();
        assertEquals(10, e.evaluate().asDouble(), 0d);
    }

    @Test(expected = EvaluationException.class)
    public void testVectorFunc3() {
        IExpression e = Entry.buildExpressionBuilder("avg([])").build();
        assertEquals(10, e.evaluate().asDouble(), 0d);
    }

    @Test(expected = EvaluationException.class)
    public void testVectorFunc4() {
        IExpression e = Entry.buildExpressionBuilder("min([])").build();
        assertEquals(10, e.evaluate().asDouble(), 0d);
    }

    @Test(expected = EvaluationException.class)
    public void testVectorFunc5() {
        IExpression e = Entry.buildExpressionBuilder("min([false, false])").build();
        assertEquals(10, e.evaluate().asDouble(), 0d);
    }

    @Test(expected = EvaluationException.class)
    public void testVectorFunc6() {
        IExpression e = Entry.buildExpressionBuilder("min([\"\", 23])").build();
        assertEquals(10, e.evaluate().asDouble(), 0d);
    }

    @Test(expected = EvaluationException.class)
    public void testVectorFunc7() {
        IExpression e = Entry.buildExpressionBuilder("max([])").build();
        assertEquals(10, e.evaluate().asDouble(), 0d);
    }

    @Test(expected = EvaluationException.class)
    public void testVectorFunc8() {
        IExpression e = Entry.buildExpressionBuilder("max([false, false])").build();
        assertEquals(10, e.evaluate().asDouble(), 0d);
    }

    @Test(expected = EvaluationException.class)
    public void testVectorFunc9() {
        IExpression e = Entry.buildExpressionBuilder("max([\"\", 23])").build();
        assertEquals(10, e.evaluate().asDouble(), 0d);
    }

    @Test
    public void testAssignment() {
        IExpressionContext context = Entry.buildContext();
        IExpression e = Entry.buildExpressionBuilder("x = y = 12").build();
        assertEquals(12, e.evaluate(context).asDouble(), 0d);
        e = Entry.buildExpressionBuilder("x * y").build();
        assertEquals(144, e.evaluate(context).asDouble(), 0d);

        e = Entry.buildExpressionBuilder("z = [12, 24, 36]").build();
        Value[] r = e.evaluate(context).asVector();
        assertEquals(3, r.length, 0d);
        assertEquals(12, r[0].asDouble(), 0d);
        assertEquals(24, r[1].asDouble(), 0d);
        assertEquals(36, r[2].asDouble(), 0d);

        e = Entry.buildExpressionBuilder("avg(z)").build();
        assertEquals(24, e.evaluate(context).asDouble(), 0d);

        e = Entry.buildExpressionBuilder("x = true").build();
        assertTrue(e.evaluate(context).asBoolean());

        e = Entry.buildExpressionBuilder("x = \"hello\"").build();
        assertEquals("hello", e.evaluate(context).asString());

        e = Entry.buildExpressionBuilder("x = [1,2,3]").build();
        assertEquals(3, e.evaluate(context).asVector().length);
    }

    @Test(expected = EvaluationException.class)
    public void testAssignmentFail() {
        IExpression e = Entry.buildExpressionBuilder("true = 12").build();
        e.evaluate();
    }

    @Test
    public void testJsonPath() throws IOException, org.json.simple.parser.ParseException {
        TestUtil util = new TestUtil();
        String json = util.loadFromFile("test.json");
        IExpressionContext context = Entry.buildContext(json);

        IExpression e = Entry.buildExpressionBuilder("$.\"%%%\"").build();
        assertEquals(42, e.evaluate(context).asLong());

        e = Entry.buildExpressionBuilder("$.\"#$@#%!!$()\"").build();
        assertEquals(22, e.evaluate(context).asLong());

        e = Entry.buildExpressionBuilder("$.k").build();
        assertEquals(119.0, e.evaluate(context).asDouble(), 0);

        e = Entry.buildExpressionBuilder("$.s.y").build();
        assertEquals(12, e.evaluate(context).asDouble(), 0);

        e = Entry.buildExpressionBuilder("$.s.u.d").build();
        assertEquals(3.14, e.evaluate(context).asDouble(), 0);

        e = Entry.buildExpressionBuilder("$.s.u.z[0]").build();
        assertEquals(11, e.evaluate(context).asDouble(), 0);

        e = Entry.buildExpressionBuilder("$.s.u.z[7]").build();
        assertTrue(e.evaluate(context).isNull());

        e = Entry.buildExpressionBuilder("$.s.u.z[7].x").build();
        assertTrue(e.evaluate(context).isNull());

        e = Entry.buildExpressionBuilder("$.s.hello").build();
        assertTrue(e.evaluate(context).isNull());

        e = Entry.buildExpressionBuilder("$.s.u.z[]").build();
        assertEquals(3, e.evaluate(context).asVector().length);

        e = Entry.buildExpressionBuilder("$.x[1].y[0]").build();
        assertEquals(2, e.evaluate(context).asDouble(), 0);

        e = Entry.buildExpressionBuilder("$.x[].y[0]").build();
        assertEquals(4, e.evaluate(context).asVector().length);

        e = Entry.buildExpressionBuilder("$.x[].y[]").build();
        assertEquals(9, e.evaluate(context).asVector().length);

        e = Entry.buildExpressionBuilder("sum($.x[].y[])").build();
        assertEquals(14, e.evaluate(context).asDouble(), 0);

        e = Entry.buildExpressionBuilder("$.x[0].z").build();
        assertEquals("hello", e.evaluate(context).asString());

        e = Entry.buildExpressionBuilder("$.x[1].z").build();
        assertEquals(false, e.evaluate(context).asBoolean());

        e = Entry.buildExpressionBuilder("$.x[]").build();
        assertTrue(e.evaluate(context).isMultiple());
        assertEquals("{\"y\": [1, 2, 3], \"z\": \"hello\"}, {\"y\": [2], \"z\": false}, {\"y\": null, \"z\": \"yeah\"}, {\"y\": [1, 2, 1, 2], \"z\": null}", e.evaluate(context).toString());

        e = Entry.buildExpressionBuilder("$.x").build();
        assertFalse(e.evaluate(context).isMultiple());
        assertEquals("[{\"y\": [1, 2, 3], \"z\": \"hello\"}, {\"y\": [2], \"z\": false}, {\"y\": null, \"z\": \"yeah\"}, {\"y\": [1, 2, 1, 2], \"z\": null}]", e.evaluate(context).toString());

        e = Entry.buildExpressionBuilder("$.empty[]").build();
        assertEquals(0, e.evaluate(context).asVector().length);

        context = Entry.buildContext("[1,2,3]");
        e = Entry.buildExpressionBuilder("$.").build();
        Value v = e.evaluate(context);
        assertNotNull(v);
        assertTrue(v.isVector());
        assertFalse(v.isMultiple());

        e = Entry.buildExpressionBuilder("$.[]").build();
        v = e.evaluate(context);
        assertTrue(v.isVector());
        assertTrue(v.isMultiple());

        e = Entry.buildExpressionBuilder("$.[1]").build();
        v = e.evaluate(context);
        assertNotNull(v);
        assertTrue(v.isNumber());
        assertEquals(2, v.asLong());
    }

    @Test
    public void testNegativeIndexJsonPath() {
        IExpressionContext context = Entry.buildContext();
        Entry.buildExpressionBuilder("x = [1,2,3,5]").build().evaluate(context);
        assertEquals(5, Entry.buildExpressionBuilder("$x[-1]").build().evaluate(context).asLong());
    }

    @Test
    public void testValueLiteral() {
        IExpression e = Entry.buildExpressionBuilder("true").build();
        assertTrue(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("false").build();
        assertFalse(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("null").build();
        assertTrue(e.evaluate().isNull());

        e = Entry.buildExpressionBuilder("\"hello\\\tworld\"").build();
        assertEquals("hello\tworld", e.evaluate().asString());
    }

    @Test(expected = EvaluationException.class)
    public void testFullValue() {
        IExpression e;
        e = Entry.buildExpressionBuilder("123").build();
        assertFalse(e.evaluate().asBoolean());
    }

    @Test(expected = EvaluationException.class)
    public void testFullValue2() {
        IExpression e;
        e = Entry.buildExpressionBuilder("123").build();
        e.evaluate().asString();
    }

    @Test(expected = EvaluationException.class)
    public void testFullValue3() {
        IExpression e;
        e = Entry.buildExpressionBuilder("123").build();
        e.evaluate().asString();
    }

    @Test(expected = EvaluationException.class)
    public void testFullValue4() {
        IExpression e;
        e = Entry.buildExpressionBuilder("true").build();
        e.evaluate().asDouble();
    }

    @Test(expected = EvaluationException.class)
    public void testFullValue5() {
        IExpression e;
        e = Entry.buildExpressionBuilder("true").build();
        e.evaluate().asLong();
    }

    @Test
    public void testFullValue6() {
        IExpression e;
        e = Entry.buildExpressionBuilder("12.4").build();
        assertEquals(12, e.evaluate().asLong());
        assertTrue(e.evaluate().isNumber());
    }

    @Test
    public void testFullValue7() {
        IExpression e;
        e = Entry.buildExpressionBuilder("12").build();
        assertEquals(12.0, e.evaluate().asDouble(), 0);
    }

    @Test
    public void testFullValue8() {
        IExpression e;
        e = Entry.buildExpressionBuilder("12").build();
        assertTrue(e.evaluate().isInteger());

        e = Entry.buildExpressionBuilder("12e2").build();
        assertTrue(e.evaluate().isDecimal());
    }

    @Test
    public void testFullValue9() {
        IExpression e;
        e = Entry.buildExpressionBuilder("12").build();
        assertTrue(e.evaluate().isNumber());
    }

    @Test
    public void testFullValue10() {
        IExpression e;
        e = Entry.buildExpressionBuilder("12").build();
        assertEquals(IntegerT, e.evaluate().getType());
    }

    @Test
    public void testFullValue11() {
        IExpression e;
        e = Entry.buildExpressionBuilder("[1,2,3]").build();
        assertEquals("[1, 2, 3]", e.evaluate().toString());
    }

    @Test(expected = ParseException.class)
    public void testFullValue12() {
        IExpression e;
        e = Entry.buildExpressionBuilder("[false, true, false 222]").build();
        e.evaluate();
    }

    @Test
    public void testLogicalOperator() {
        IExpression e = Entry.buildExpressionBuilder("!true").build();
        assertFalse(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("! false").build();
        assertTrue(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("true && true").build();
        assertTrue(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("true && false").build();
        assertFalse(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("true || true").build();
        assertTrue(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("false || true").build();
        assertTrue(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("false || false").build();
        assertFalse(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("false || true && !false").build();
        assertTrue(e.evaluate().asBoolean());
    }

    @Test
    public void testRationalOperator() {
        IExpression e;
        e = Entry.buildExpressionBuilder("2 > 1").build();
        assertTrue(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("2 >= 1").build();
        assertTrue(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("2 < 1").build();
        assertFalse(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("2 <= 2").build();
        assertTrue(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("2 == 1").build();
        assertFalse(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("2 != 1").build();
        assertTrue(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("true != false").build();
        assertTrue(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("\"abc\" < \"def\"").build();
        assertTrue(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("\"hello\" != \" hello\"").build();
        assertTrue(e.evaluate().asBoolean());
    }

    @Test
    public void testJsonType() {
        IExpression e;
        e = Entry.buildExpressionBuilder("[true, false]").build();
        assertTrue(e.evaluate().isVector());
        assertTrue(e.evaluate().isHomogeneousVector());
        assertEquals(Value.Type.BooleanT, e.evaluate().getHomogeneousType());

        e = Entry.buildExpressionBuilder("[\"true\", \"false\"]").build();
        assertTrue(e.evaluate().isVector());
        assertTrue(e.evaluate().isHomogeneousVector());
        assertEquals(Value.Type.StringT, e.evaluate().getHomogeneousType());

        e = Entry.buildExpressionBuilder("[true, \"false\"]").build();
        assertTrue(e.evaluate().isVector());
        assertFalse(e.evaluate().isHomogeneousVector());

        e = Entry.buildExpressionBuilder("[null, null]").build();
        assertTrue(e.evaluate().isVector());
        assertTrue(e.evaluate().isHomogeneousVector());
        assertEquals(NullT, e.evaluate().getHomogeneousType());

        e = Entry.buildExpressionBuilder("null").build();
        assertTrue(e.evaluate().isNull());

        e = Entry.buildExpressionBuilder("true").build();
        assertTrue(e.evaluate().isBoolean());

        e = Entry.buildExpressionBuilder("\"\"").build();
        assertTrue(e.evaluate().isString());

        e = Entry.buildExpressionBuilder("[]").build();
        assertTrue(e.evaluate().isVector());
        assertEquals(0, e.evaluate().asVector().length);
    }

    @Test
    public void testExpInVectorOrMap() {
        IExpression e;
        e = Entry.buildExpressionBuilder("[-1, 22, -99, +43]").build();
        assertTrue(e.evaluate().isVector());
        assertEquals(4, e.evaluate().asVector().length);

        e = Entry.buildExpressionBuilder("{\"x\": -22, \"y\": +43, \"z\": 1+1}").build();
        assertTrue(e.evaluate().isMap());
        assertEquals(3, e.evaluate().asMap().size());
    }

    @Test
    public void testContextValue() {
        IExpressionContext context = Entry.buildContext();
        context.updateVariable("b", false);
        context.updateVariable("s", "hello");
        context.updateVariable("n", null);
        context.updateVariable("l", 12L);
        context.updateVariable("d", 12d);

        assertEquals(BooleanT, Entry.buildExpressionBuilder("b").build().evaluate(context).getType());
        assertEquals(StringT, Entry.buildExpressionBuilder("s").build().evaluate(context).getType());
        assertEquals(NullT, Entry.buildExpressionBuilder("n").build().evaluate(context).getType());
        assertEquals(IntegerT, Entry.buildExpressionBuilder("l").build().evaluate(context).getType());
        assertEquals(DecimalT, Entry.buildExpressionBuilder("d").build().evaluate(context).getType());
    }

    @Test
    public void testToNumberFunc1() {
        IExpression e;
        e = Entry.buildExpressionBuilder("toNumber(true)").build();
        assertEquals(1, e.evaluate().asLong());

        e = Entry.buildExpressionBuilder("toNumber(false)").build();
        assertEquals(0, e.evaluate().asLong());

        e = Entry.buildExpressionBuilder("toNumber(12)").build();
        assertEquals(12, e.evaluate().asLong());

        e = Entry.buildExpressionBuilder("toNumber(12.1)").build();
        assertEquals(12.1, e.evaluate().asDouble(), 0f);

        e = Entry.buildExpressionBuilder("toNumber(\"123\")").build();
        assertEquals(123, e.evaluate().asDouble(), 0f);

        e = Entry.buildExpressionBuilder("toNumber(\"12.3\")").build();
        assertEquals(12.3, e.evaluate().asDouble(), 0f);

        Value[] vs = new Value[]{Value.of(12), Value.of(23d), Value.of(0), Value.of(11.3)};
        e = Entry.buildExpressionBuilder("toNumber([12, \"23\", false, 11.3])").build();
        assertArrayEquals(vs, e.evaluate().asVector());

        e = Entry.buildExpressionBuilder("toNumber(\"12,345\")").build();
        assertEquals(12345, e.evaluate().asDouble(), 0f);
    }

    @Test(expected = EvaluationException.class)
    public void testToNumberFunc2() {
        IExpression e;
        e = Entry.buildExpressionBuilder("toNumber(null)").build();
        e.evaluate();
    }

    @Test(expected = EvaluationException.class)
    public void testToNumberFunc3() {
        IExpression e;
        e = Entry.buildExpressionBuilder("toNumber(\"bad number\")").build();
        e.evaluate();
    }

    @Test(expected = EvaluationException.class)
    public void testToNumberFunc4() {
        IExpression e;
        e = Entry.buildExpressionBuilder("toNumber(\"123,def\")").build();
        e.evaluate();
    }

    @Test
    public void testToStringFunc1() {
        IExpression e;
        e = Entry.buildExpressionBuilder("toString(true)").build();
        assertEquals("true", e.evaluate().asString());
        e = Entry.buildExpressionBuilder("toString(false)").build();
        assertEquals("false", e.evaluate().asString());
        e = Entry.buildExpressionBuilder("toString(12)").build();
        assertEquals("12", e.evaluate().asString());
        e = Entry.buildExpressionBuilder("toString(12.4)").build();
        assertEquals("12.4", e.evaluate().asString());
        e = Entry.buildExpressionBuilder("toString(null)").build();
        assertEquals("null", e.evaluate().asString());
        e = Entry.buildExpressionBuilder("toString(\"hello\n\")").build();
        assertEquals("\"hello\\n\"", e.evaluate().asString());

        Value[] vs = new Value[] {Value.of("12"), Value.of("false"), Value.of("null")};
        e = Entry.buildExpressionBuilder("toString([12, false, null])").build();
        assertArrayEquals(vs, e.evaluate().asVector());
    }

    @Test
    public void testToBooleanFunc1() {
        IExpression e;
        e = Entry.buildExpressionBuilder("toBoolean(\"true\")").build();
        assertTrue(e.evaluate().asBoolean());
        e = Entry.buildExpressionBuilder("toBoolean(\"false\")").build();
        assertFalse(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("toBoolean(0)").build();
        assertFalse(e.evaluate().asBoolean());
        e = Entry.buildExpressionBuilder("toBoolean(0.0)").build();
        assertFalse(e.evaluate().asBoolean());

        e = Entry.buildExpressionBuilder("toBoolean(12)").build();
        assertTrue(e.evaluate().asBoolean());
        e = Entry.buildExpressionBuilder("toBoolean(-12)").build();
        assertTrue(e.evaluate().asBoolean());

        Value[] vs = new Value[]{Value.TRUE, Value.FALSE, Value.FALSE, Value.TRUE};
        e = Entry.buildExpressionBuilder("toBoolean([12, false, 0, \"true\"])").build();
        assertArrayEquals(vs, e.evaluate().asVector());
    }

    @Test(expected = EvaluationException.class)
    public void testToBooleanFunc2() {
        IExpression e;
        e = Entry.buildExpressionBuilder("toBoolean(\"bad number\")").build();
        e.evaluate();
    }

    @Test(expected = EvaluationException.class)
    public void testToBooleanFunc3() {
        IExpression e;
        e = Entry.buildExpressionBuilder("toBoolean(null)").build();
        e.evaluate();
    }

    @Test
    public void testChoice() {
        IExpression e;
        e = Entry.buildExpressionBuilder("choice(3 > 2, \"hello\", 42)").build();
        assertEquals("hello", e.evaluate().asString());
        e = Entry.buildExpressionBuilder("choice(3 < 2, \"hello\", 42)").build();
        assertEquals(42, e.evaluate().asLong());
    }

    @Test
    public void testAtBlock() {
        IExpression e;
        e = Entry.buildExpressionBuilder("filter([12,23,44,31], @{_ > 20})").build();
        assertEquals(3, e.evaluate().asVector().length);

        e = Entry.buildExpressionBuilder("filter([12,23,44,31], @{_ > 20 && _ < 30})").build();
        assertEquals(1, e.evaluate().asVector().length);

        e = Entry.buildExpressionBuilder("filter([12,23,44,31], @{_ > 100})").build();
        assertEquals(0, e.evaluate().asVector().length);
    }

    @Test
    public void testMap() {
        IExpression e;
        e = Entry.buildExpressionBuilder("{}").build();
        assertEquals(0, e.evaluate().asMap().size());

        e = Entry.buildExpressionBuilder("{ \"x\" : 12, \"y\":[], \"z\": {  }  }").build();
        assertEquals("{\"x\": 12, \"y\": [], \"z\": {}}", e.evaluate().toString());
    }

    @Test
    public void testJsonFilter() throws IOException, org.json.simple.parser.ParseException {
        TestUtil util = new TestUtil();
        String json = util.loadFromFile("test.json");
        IExpressionContext context = Entry.buildContext(json);

        IExpression e;
        e = Entry.buildExpressionBuilder("$.x[@{$.z == \"hello\" || $.z == \"yeah\"}]").build();
        assertTrue(e.evaluate(context).isMultiple());

        e = Entry.buildExpressionBuilder("$.x[@{$.z == \"hello\"}]").build();
        assertEquals(1, e.evaluate(context).asVector().length);

        e = Entry.buildExpressionBuilder("$.x[@{$.z == \"hellossssss\"}]").build();
        assertEquals(0, e.evaluate(context).asVector().length);

        e = Entry.buildExpressionBuilder("filter($.x, @{$.z == \"hello\"})").build();
        assertEquals(1, e.evaluate(context).asVector().length);
    }

    @Test
    public void testJsonGet() {
        IExpressionContext context = Entry.buildContext();

        IExpression e;
        Entry.buildExpressionBuilder("x = [1,2,3]").build().evaluate(context);
        e = Entry.buildExpressionBuilder("jsonGet(x, \"$[]\")").build();
        assertTrue(e.evaluate(context).isVector());
        e = Entry.buildExpressionBuilder("jsonGet(x, \"$[2]\")").build();
        assertEquals(3, e.evaluate(context).asLong());

        Entry.buildExpressionBuilder("y = {\"a\": 212, \"b\": [false, true]}").build().evaluate(context);
        e = Entry.buildExpressionBuilder("jsonGet(y, \"$.a\")").build();
        assertEquals(212, e.evaluate(context).asLong());

        e = Entry.buildExpressionBuilder("jsonGet(y, \"$.b[1]\")").build();
        assertTrue(e.evaluate(context).asBoolean());
    }

    @Test
    public void testTemplate() throws org.json.simple.parser.ParseException {
        IExpressionContext context = Entry.buildContext("{\"x\": 12}");

        IExpression e;
        e = Entry.buildExpressionBuilder("`x=$.x`").build();
        assertEquals("x=12", e.evaluate(context).asString());

        context = Entry.buildContext("{\"x\": \"12\"}");
        e = Entry.buildExpressionBuilder("`x=$.x`").build();
        assertEquals("x=12", e.evaluate(context).asString());

        e = Entry.buildExpressionBuilder("`\\$x=$.x`").build();
        assertEquals("$x=12", e.evaluate(context).asString());

        e = Entry.buildExpressionBuilder("`\\$x=$.x`").build();
        assertEquals("$x=12", e.evaluate(context).asString());

        context = Entry.buildContext("{\"x\": 12}");
        e = Entry.buildExpressionBuilder("`\\\\x=$.x \\`$.x\\` x`").build();
        assertEquals("\\x=12 `12` x", e.evaluate(context).asString());

        e = Entry.buildExpressionBuilder("`x=${.x} ${.x}$.x`").build();
        assertEquals("x=12 1212", e.evaluate(context).asString());

        e = Entry.buildExpressionBuilder("``").build();
        assertEquals("", e.evaluate(context).asString());

        Entry.buildExpressionBuilder("x = false").build().evaluate(context);
        Entry.buildExpressionBuilder("y = 42").build().evaluate(context);
        e = Entry.buildExpressionBuilder("`x is $x, y is $y`").build();
        assertEquals("x is false, y is 42", e.evaluate(context).asString());
    }

    private static boolean compareListsIgnoreOrder(Long[] list1, Value[] list2) {
        if (list1.length != list2.length) {
            return false;
        }

        Arrays.sort(list1);
        Long[] list3 = Arrays.stream(list2).map(Value::asLong).toArray(Long[]::new);
        Arrays.sort(list3);

        return Arrays.equals(list1, list3);
    }

    @Test
    public void testSetOps() {
        IExpression e;
        e = Entry.buildExpressionBuilder("union([1,2,3,4], [3,4,5,6])").build();
        assertTrue(compareListsIgnoreOrder(new Long[]{1L, 2L, 3L, 4L, 5L, 6L}, e.evaluate().asVector()));

        e = Entry.buildExpressionBuilder("intersect([1,2,3,4], [3,4,5,6])").build();
        assertTrue(compareListsIgnoreOrder(new Long[]{3L, 4L}, e.evaluate().asVector()));

        e = Entry.buildExpressionBuilder("diff([1,2,3,4], [3,4,5,6])").build();
        assertTrue(compareListsIgnoreOrder(new Long[]{1L, 2L}, e.evaluate().asVector()));

        e = Entry.buildExpressionBuilder("symDiff([1,2,3,4], [3,4,5,6])").build();
        assertTrue(compareListsIgnoreOrder(new Long[]{1L, 2L, 5L, 6L}, e.evaluate().asVector()));
    }

    @Test(expected = EvaluationException.class)
    public void testSetOpsFailure() {
        Entry.buildExpressionBuilder("union([1,2,3,4], false)").build().evaluate();
    }

    @Test(expected = EvaluationException.class)
    public void testSetOpsFailure2() {
        Entry.buildExpressionBuilder("intersect([1,2,3,4], false)").build().evaluate();
    }

    @Test(expected = EvaluationException.class)
    public void testSetOpsFailure3() {
        Entry.buildExpressionBuilder("diff([1,2,3,4], false)").build().evaluate();
    }

    @Test(expected = EvaluationException.class)
    public void testSetOpsFailure4() {
        Entry.buildExpressionBuilder("symDiff([1,2,3,4], false)").build().evaluate();
    }

    @Test
    public void testArraySort() {
        IExpression e;
        e = Entry.buildExpressionBuilder("[3,4,5.0,6,1,2].sort()").build();
        assertEquals("[1, 2, 3, 4, 5.0, 6]", e.evaluate().toString());

        e = Entry.buildExpressionBuilder("sort([3,4,5.0,6,1,2])").build();
        assertEquals("[1, 2, 3, 4, 5.0, 6]", e.evaluate().toString());

        e = Entry.buildExpressionBuilder("sort([3,4,5.0,6,1,2], @{a<=>b})").build();
        assertEquals("[1, 2, 3, 4, 5.0, 6]", e.evaluate().toString());

        e = Entry.buildExpressionBuilder("[3,4,5.0,6,1,2].sort(@{a<=>b})").build();
        assertEquals("[1, 2, 3, 4, 5.0, 6]", e.evaluate().toString());

        e = Entry.buildExpressionBuilder("sort([3,4,5,6,1,2], @{ b <=> a })").build();
        assertEquals("[6, 5, 4, 3, 2, 1]", e.evaluate().toString());

        e = Entry.buildExpressionBuilder("sort([\"def\", \"abc\"])").build();
        assertEquals("[\"abc\", \"def\"]", e.evaluate().toString());

        e = Entry.buildExpressionBuilder("sort([false, true, false, true])").build();
        assertEquals("[false, false, true, true]", e.evaluate().toString());

        e = Entry.buildExpressionBuilder("sort([{\"x\":{\"y\": 2}}, {\"x\":{\"y\": 3}}, {\"x\":{\"y\": 1}}], @{ $a.x.y <=> $b.x.y })").build();
        assertEquals("[{\"x\": {\"y\": 1}}, {\"x\": {\"y\": 2}}, {\"x\": {\"y\": 3}}]", e.evaluate().toString());

        e = Entry.buildExpressionBuilder("sort(toDate([\"2020-01-01\", \"2023-01-01T12:01\"]), @{b <=> a})").build();
        assertEquals("[\"2023-01-01T12:01\", \"2020-01-01\"]", e.evaluate().toString());

        e = Entry.buildExpressionBuilder("sort(toDateFmt([\"2020-01-01\", \"2023-01-01\"], \"yyyy-MM-dd\"), @{b <=> a})").build();
        assertEquals("[\"2023-01-01\", \"2020-01-01\"]", e.evaluate().toString());
    }

    @Test(expected = EvaluationException.class)
    public void testArraySortFailure() {
        Entry.buildExpressionBuilder("sort([3,4,5,false,1,2])").build().evaluate();
    }

    @Test(expected = EvaluationException.class)
    public void testArraySortFailure2() {
        Entry.buildExpressionBuilder("sort([3,4,5,4,1,2], 123)").build().evaluate();
    }

    @Test
    public void testConcat() {
        IExpression e;
        IExpressionContext context = Entry.buildContext();
        Entry.buildExpressionBuilder("x=1+2").build().evaluate(context);
        Entry.buildExpressionBuilder("y = true || false").build().evaluate(context);
        e = Entry.buildExpressionBuilder("concat(x,y, 4/2)").build();
        assertEquals("[3, true, 2.0]", e.evaluate(context).toString());
    }

    @Test
    public void testJoin() {
        IExpression e;
        IExpressionContext context = Entry.buildContext();
        Entry.buildExpressionBuilder("x=[{\"id\": 1, \"x\": 42}]").build().evaluate(context);
        Entry.buildExpressionBuilder("y=[{\"id2\": 1, \"y\": 42, \"x\": 56}]").build().evaluate(context);
        e = Entry.buildExpressionBuilder("join(x, y, \"id\", \"id2\")").build();
        Value v = e.evaluate(context);
        assertTrue(v.isVector() && v.asVector().length == 1);
        assertEquals(1, v.asVector()[0].asMap().get("id").asLong());
        assertEquals(1, v.asVector()[0].asMap().get("id2").asLong());
        assertEquals(56, v.asVector()[0].asMap().get("x").asLong());
        assertEquals(42, v.asVector()[0].asMap().get("y").asLong());
    }

    @Test
    public void testFilterFunc() {
        Value v;
        v = Entry.buildExpressionBuilder("[1,2,3,4,5].filter(@{_ >3 })").build().evaluate();
        assertEquals(2, v.asVector().length);

        v = Entry.buildExpressionBuilder("[].length() > 2").build().evaluate();
        assertFalse(v.asBoolean());

        v = Entry.buildExpressionBuilder("[[],[2,3],[4],[5,6,7]].filter(@{_.length() >2 })").build().evaluate();
        assertEquals(1, v.asVector().length);
    }

    @Test
    public void testMapFunc() {
        IExpression e;
        Value v;
        IExpressionContext context = Entry.buildContext();
        e = Entry.buildExpressionBuilder("map([1,2,3,4], @{_^2})").build();
        v = e.evaluate(context);
        assertTrue(v.isVector() && v.asVector().length == 4);
        assertEquals(9, v.asVector()[2].asLong());

        v = Entry.buildExpressionBuilder("[[],[2,3],[4],[5,6,7]].map(@{length(_)})").build().evaluate();
        assertEquals(4, v.asVector().length);

        Entry.buildExpressionBuilder("x = [1,2,3]").build().evaluate(context);
        Entry.buildExpressionBuilder("y = [{\"x\":1}, {\"x\":4}, {\"x\":2}]").build().evaluate(context);
        Entry.buildExpressionBuilder("z = @{$.x == _}").build().evaluate(context);
        e = Entry.buildExpressionBuilder("map(x, @{i=_; $y[@{$.x == i+1}]}).filter(@{_.length() != 0})").build();
//        e = Entry.buildExpressionBuilder("map(x, @{i=_; $y[@{$.x == i+1}]}).filter(@{true})").build();
        v = e.evaluate(context);
        assertEquals("[{\"x\": 2}, {\"x\": 4}]", v.toString());

        e = Entry.buildExpressionBuilder("filter(y, @{(x+1).contains($_.x)})").build();
        v = e.evaluate(context);
        assertEquals("[{\"x\": 4}, {\"x\": 2}]", v.toString());
    }

    @Test
    public void testNow() {
        IExpression e;
        IExpressionContext context = Entry.buildContext();
        e = Entry.buildExpressionBuilder("now()").build();
        Value v = e.evaluate(context);
        assertTrue(v.isDateTime());
        assertTrue(v.toString().length() > 0);
    }

    @Test
    public void testDefaultDate() {
        Entry.buildExpressionBuilder("toDate(\"2023/12/12\")").build().evaluate();
        Entry.buildExpressionBuilder("toDate(\"2023-12-12\")").build().evaluate();
        Entry.buildExpressionBuilder("toDate(\"2023.12.12\")").build().evaluate();
        Entry.buildExpressionBuilder("toDate(\"2023/12/12 12:23:04\")").build().evaluate();
        Entry.buildExpressionBuilder("toDate(\"2023-12-12 12:23:04\")").build().evaluate();
    }

    @Test(expected = EvaluationException.class)
    public void testDefaultDateError() {
        Entry.buildExpressionBuilder("toDate(\"2023 12 12\")").build().evaluate();
    }

    @Test
    public void testCustomDate() {
        Entry.buildExpressionBuilder("toDateFmt(\"2023 12 12\", \"yyyy MM dd\")").build().evaluate();
    }

    @Test(expected = ParseException.class)
    public void testConcatFail() {
        IExpression e;
        e = Entry.buildExpressionBuilder("1 concat(2,3,4)").build();
        assertEquals("[1, 2, 3, 4]", e.evaluate(Entry.buildContext()).toString());
    }

    @Test(expected = ParseException.class)
    public void testBadMap() {
        Entry.buildExpressionBuilder("{\"x\":12, \"y\", 12}").build().evaluate();
    }

    @Test(expected = ParseException.class)
    public void testBadMap2() {
        Entry.buildExpressionBuilder("{\"x\":12, \"y\" }").build().evaluate();
    }

    @Test
    public void testAtExp() {
        IExpression e;
        IExpressionContext context = Entry.buildContext();
        Entry.buildExpressionBuilder("x=@{_+12}").build().evaluate(context);
        e = Entry.buildExpressionBuilder("x(12)").build();
        assertEquals("24", e.evaluate(context).toString());
    }

    @Test
    public void testVectorAdd() {
        IExpression e;
        e = Entry.buildExpressionBuilder("[1, null] ++ [false, 4]").build();
        assertEquals("[1, null, false, 4]", e.evaluate().toString());
    }

    @Test
    public void testListUniq() {
        IExpression e;
        e = Entry.buildExpressionBuilder("uniq([3,4,4,5,6,6,6,1,2])").build();
        assertEquals("[3, 4, 5, 6, 1, 2]", e.evaluate().toString());

        e = Entry.buildExpressionBuilder("uniq([{\"x\":1,\"y\":3}, {\"x\":2,\"y\":3}, {\"x\":2,\"y\":5}, {\"x\":3,\"y\":3}], @{choice($a.x == $b.x, choice($a.y < $b.y, 0, -1), 1)})").build();
        assertEquals("[{\"x\": 1, \"y\": 3}, {\"x\": 2, \"y\": 5}, {\"x\": 3, \"y\": 3}]", e.evaluate().toString());

        IExpressionContext context = Entry.buildContext();
        e = Entry.buildExpressionBuilder("xxx = [{\"x\":1,\"y\":3}, {\"x\":2,\"y\":3}, {\"x\":2,\"y\":5}, {\"x\":3,\"y\":3}]").build();
        e.evaluate(context);
        e = Entry.buildExpressionBuilder("xxx.uniq(@{choice($a.x == $b.x, choice($a.y < $b.y, 0, -1), 1)})").build();
        assertEquals("[{\"x\": 1, \"y\": 3}, {\"x\": 2, \"y\": 5}, {\"x\": 3, \"y\": 3}]", e.evaluate(context).toString());
    }

    @Test(expected = EvaluationException.class)
    public void testListUniqFail() {
        Entry.buildExpressionBuilder("uniq([false,4,4,5,6,6,6,1,2])").build().evaluate();
    }

    @Test
    public void testJsonVarAccess() throws org.json.simple.parser.ParseException {
        IExpression e;
        IExpressionContext context = Entry.buildContext();
        Entry.buildExpressionBuilder("x=[false, 2.2, \"hello\"]").build().evaluate(context);
        e = Entry.buildExpressionBuilder("$x[1]").build();
        assertEquals("2.2", e.evaluate(context).toString());
        e = Entry.buildExpressionBuilder("$x[0]").build();
        assertEquals("false", e.evaluate(context).toString());

        Entry.buildExpressionBuilder("x={\"f\": false, \"v\": 2.2, \"n\": \"hello\"}").build().evaluate(context);
        e = Entry.buildExpressionBuilder("$x.f").build();
        assertEquals("false", e.evaluate(context).toString());

        Entry.buildExpressionBuilder("x=12").build().evaluate(context);
        e = Entry.buildExpressionBuilder("$x").build();
        assertEquals("12", e.evaluate(context).toString());

        Entry.buildExpressionBuilder("xyz=true").build().evaluate(context);
        e = Entry.buildExpressionBuilder("${xyz}").build();
        assertEquals("true", e.evaluate(context).toString());

        context = Entry.buildContext("[11, 22, 33]");
        Entry.buildExpressionBuilder("x = 12").build().evaluate(context);
        Entry.buildExpressionBuilder("y = [x, x^2, x/x]").build().evaluate(context);
        e = Entry.buildExpressionBuilder("$y[1]").build();
        assertEquals(144, e.evaluate(context).asDouble(), 0);
        e = Entry.buildExpressionBuilder("$[1]").build();
        assertEquals(22, e.evaluate(context).asDouble(), 0);
    }

    @Test
    public void testShortCircuitOperator() {
        IExpression e;
        IExpressionContext context = Entry.buildContext();
        Entry.buildExpressionBuilder("x=null").build().evaluate(context);
        e = Entry.buildExpressionBuilder("x != null && x > 1").build();
        assertEquals("false", e.evaluate(context).toString());

        Entry.buildExpressionBuilder("y=12").build().evaluate(context);
        e = Entry.buildExpressionBuilder("y > 12 && x != null && x > 1").build();
        assertEquals("false", e.evaluate(context).toString());

        Entry.buildExpressionBuilder("z=4").build().evaluate(context);
        e = Entry.buildExpressionBuilder("y >= 12 && x == null && z > 1").build();
        assertEquals("true", e.evaluate(context).toString());

        Entry.buildExpressionBuilder("z=4").build().evaluate(context);
        e = Entry.buildExpressionBuilder("z >= 1 || x > 1").build();
        assertEquals("true", e.evaluate(context).toString());

        e = Entry.buildExpressionBuilder("!(z >= 1 || x > 1)").build();
        assertEquals("false", e.evaluate(context).toString());
    }

    @Test(expected = ParseException.class)
    public void testVectorConstruct() {
        IExpression e = Entry.buildExpressionBuilder("[12 false true \"23\"]").build();
        e.evaluate();
    }

    @Test
    public void testParentheses() {
        IExpression e = Entry.buildExpressionBuilder("-(-1)").build();
        assertEquals(1, e.evaluate().asLong());
    }

    @Test
    public void testParentheses1() {
        IExpression e = Entry.buildExpressionBuilder("(-1)").build();
        assertEquals(-1, e.evaluate().asLong());
    }

    @Test(expected = ParseException.class)
    public void testParentheses2() {
        Entry.buildExpressionBuilder("logb(2+3 5-1)").build().evaluate();
    }

    @Test(expected = EvaluationException.class)
    public void testFuncNoEnoughParam() {
        Entry.buildExpressionBuilder("logb(12)").build().evaluate();
    }

    @Test(expected = EvaluationException.class)
    public void testFuncTooManyParams() {
        Entry.buildExpressionBuilder("logb(12, 13, 14)").build().evaluate();
    }

    @Test(expected = ParseException.class)
    public void testOperatorNoEnoughParam() {
        Entry.buildExpressionBuilder("12+").build().evaluate();
    }

    @Test(expected = EvaluationException.class)
    public void testExpressionCall() {
        IExpression e;
        IExpressionContext context = Entry.buildContext();
        Entry.buildExpressionBuilder("x=@{_+12}").build().evaluate(context);
        e = Entry.buildExpressionBuilder("x(23, 22)").build();
        e.evaluate(context);
    }

    @Test(expected = ParseException.class)
    public void testMapComma() {
        Entry.buildExpressionBuilder("{\"x\":12(\"y\"}").build().evaluate();
    }

    @Test(expected = ParseException.class)
    public void testBadSeparator() {
        Entry.buildExpressionBuilder(",12").build().evaluate();
    }

    @Test(expected = ParseException.class)
    public void testBadMapSeparator() {
        Entry.buildExpressionBuilder("{12:12}").build().evaluate();
    }

    @Test(expected = ParseException.class)
    public void testBadMapSeparator2() {
        Entry.buildExpressionBuilder("[\"12\":12]").build().evaluate();
    }

    @Test(expected = ParseException.class)
    public void testBadMapSeparator3() {
        Entry.buildExpressionBuilder(":").build().evaluate();
    }

    @Test
    public void testScalable() {
        assertEquals("[1.0, 4.0, 9.0]", Entry.buildExpressionBuilder("pow([1,2,3], 2)").build().evaluate().toString());
        assertEquals("[2, 4, 6]", Entry.buildExpressionBuilder("[1,2,3] * 2").build().evaluate().toString());
        assertEquals("[2, 4, 6]", Entry.buildExpressionBuilder("2 * [1,2,3]").build().evaluate().toString());
        assertEquals("[1, 4, 9]", Entry.buildExpressionBuilder("[1,2,3] * [1,2,3]").build().evaluate().toString());
    }

    @Test(expected = EvaluationException.class)
    public void testScalableException() {
        assertEquals("[1, 4, 9]", Entry.buildExpressionBuilder("[1,2,3] * [1]").build().evaluate().toString());
    }

    @Test(expected = EvaluationException.class)
    public void testCustomDateFmt() {
        assertTrue(Entry.buildExpressionBuilder("toDateFmt(\"2022 9 1\", \"yyyy M d\")").build().evaluate().isDateTime());
        assertTrue(Entry.buildExpressionBuilder("toDateFmt(\"2022 12 1\", \"yyyy M d\")").build().evaluate().isDateTime());
        assertTrue(Entry.buildExpressionBuilder("toDateFmt(\"2022 9 1\", \"yyyy MM d\")").build().evaluate().isDateTime());
    }

    @Test
    public void testRegMatch() {
        assertTrue(Entry.buildExpressionBuilder("regMatch(\"2022\", \"\\\\\\\\d+\")").build().evaluate().asBoolean());
        assertFalse(Entry.buildExpressionBuilder("regMatch(\"d2022\", \"\\\\\\\\d+\")").build().evaluate().asBoolean());
        assertTrue(Entry.buildExpressionBuilder("regMatch(\"d2022\", \".*\\\\\\\\d+\")").build().evaluate().asBoolean());
        assertFalse(Entry.buildExpressionBuilder("regMatch(null, \".*\\\\\\\\d+\")").build().evaluate().asBoolean());
    }

    @Test(expected = EvaluationException.class)
    public void testRegMatchExp() {
        Entry.buildExpressionBuilder("regMatch(\"d2022\", \"*\\\\\\\\d+\")").build().evaluate();
    }

    @Test
    public void testStringAdd() {
        assertEquals("Hello world", Operators.addition(Value.of("Hello"), Value.of(" world")).asString());
    }

    @Test(expected = EvaluationException.class)
    public void testValueAddException() {
        Operators.addition(Value.of(false), Value.of());
    }

    @Test
    public void testDateAdd() {
        String dt = "2023-01-02T13:23:14";
        Value.DateWithFmt dwf = new Value.DateWithFmt(LocalDateTime.parse(dt), Value.DEFAULT_DATETIME_FMT);
        assertEquals(LocalDateTime.parse("2024-01-02T13:23:14"), Operators.addition(Value.of(dwf), Value.of("1y")).asDateTime());
        assertEquals(LocalDateTime.parse("2023-03-02T13:23:14"), Operators.addition(Value.of(dwf), Value.of("2M")).asDateTime());
        assertEquals(LocalDateTime.parse("2023-01-05T13:23:14"), Operators.addition(Value.of(dwf), Value.of("3d")).asDateTime());
        assertEquals(LocalDateTime.parse("2023-01-02T17:23:14"), Operators.addition(Value.of(dwf), Value.of("4h")).asDateTime());
        assertEquals(LocalDateTime.parse("2023-01-02T13:27:14"), Operators.addition(Value.of(dwf), Value.of("4m")).asDateTime());
        assertEquals(LocalDateTime.parse("2023-01-02T13:23:20"), Operators.addition(Value.of(dwf), Value.of("6s")).asDateTime());
        assertEquals("\"2024-03-05T17:27:20\"", Entry.buildExpressionBuilder("toDate(\"2023-01-02T13:23:14\")+\"1y\"+\"2M\"+\"3d\"+\"4h\"+\"4m\"+\"6s\"").build().evaluate().toString());
    }

    @Test(expected = EvaluationException.class)
    public void testDateAddException() {
        String dt = "2023-01-02T13:23:14";
        assertEquals(LocalDateTime.parse("2024-01-02T13:23:14"), Operators.addition(Value.of(new Value.DateWithFmt(LocalDateTime.parse(dt), dt)), Value.of("1Y")).asDateTime());
    }

    @Test
    public void testDateSub() {
        String dt = "2023-01-02T13:23:14";
        Value.DateWithFmt dwf = new Value.DateWithFmt(LocalDateTime.parse(dt), Value.DEFAULT_DATETIME_FMT);
        assertEquals(LocalDateTime.parse("2022-01-02T13:23:14"), Operators.subtraction(Value.of(dwf), Value.of("1y")).asDateTime());
        assertEquals(LocalDateTime.parse("2022-11-02T13:23:14"), Operators.subtraction(Value.of(dwf), Value.of("2M")).asDateTime());
        assertEquals(LocalDateTime.parse("2022-12-30T13:23:14"), Operators.subtraction(Value.of(dwf), Value.of("3d")).asDateTime());
        assertEquals(LocalDateTime.parse("2023-01-02T09:23:14"), Operators.subtraction(Value.of(dwf), Value.of("4h")).asDateTime());
        assertEquals(LocalDateTime.parse("2023-01-02T13:19:14"), Operators.subtraction(Value.of(dwf), Value.of("4m")).asDateTime());
        assertEquals(LocalDateTime.parse("2023-01-02T13:23:08"), Operators.subtraction(Value.of(dwf), Value.of("6s")).asDateTime());
        assertEquals(1, Operators.subtraction(Value.of(new Value.DateWithFmt(LocalDateTime.parse(dt), dt)),
                Value.of(new Value.DateWithFmt(LocalDateTime.parse("2023-01-01T13:23:14"), Value.DEFAULT_DATETIME_FMT))).asLong());
        assertEquals("2021-10-30T09:19:08",
                Entry.buildExpressionBuilder("toDate(\"2023-01-02T13:23:14\")-\"1y\"-\"2M\"-\"3d\"-\"4h\"-\"4m\"-\"6s\"").build().evaluate().asDateTime().toString());
        assertEquals("2021-12-23T12:12:12",
                Entry.buildExpressionBuilder("toDate(\"2023/12/23 12:12:12\") - \"2y\"").build().evaluate().asDateTime().toString());
    }

    @Test(expected = EvaluationException.class)
    public void testDateSubException() {
        String dt = "2023-01-02T13:23:14";
        Operators.subtraction(Value.of(new Value.DateWithFmt(LocalDateTime.parse(dt), dt)), Value.of("2Y"));
    }

    @Test(expected = EvaluationException.class)
    public void testValueSubException() {
        String dt = "2023-01-02T13:23:14";
        Operators.subtraction(Value.of(dt), Value.of("2Y"));
    }

    @Test
    public void testTake() {
        assertEquals(2, Entry.buildExpressionBuilder("take([1,2,3,4,5,6,7], 2)").build().evaluate().asVector().length);
        assertEquals(7, Entry.buildExpressionBuilder("take([1,2,3,4,5,6,7], 8)").build().evaluate().asVector().length);
        assertEquals(0, Entry.buildExpressionBuilder("take([1,2,3,4,5,6,7], 0)").build().evaluate().asVector().length);
        assertEquals(0, Entry.buildExpressionBuilder("take([1,2,3,4,5,6,7], -100)").build().evaluate().asVector().length);
    }

    @Test
    public void testKeys() {
        assertEquals(2, Entry.buildExpressionBuilder("keys({\"x\":1, \"y\":2})").build().evaluate().asVector().length);
        assertEquals(3, Entry.buildExpressionBuilder("keys({\"x\":1, \"y\":2, \"z\":3})").build().evaluate().asVector().length);
        assertEquals(2, Entry.buildExpressionBuilder("keys([{}, {}])").build().evaluate().asVector().length);
        assertEquals(0, Entry.buildExpressionBuilder("keys({})").build().evaluate().asVector().length);
    }

    @Test
    public void testValues() {
        assertEquals(2, Entry.buildExpressionBuilder("values({\"x\":1, \"y\":2})").build().evaluate().asVector().length);
        assertEquals(3, Entry.buildExpressionBuilder("values({\"x\":1, \"y\":2, \"z\":3})").build().evaluate().asVector().length);
        assertEquals(0, Entry.buildExpressionBuilder("values([])").build().evaluate().asVector().length);
    }

    @Test
    public void testCodeBlockAccessGlobalContext() throws org.json.simple.parser.ParseException {
        String json = "{\"x\":12,\"y\":[11,12,13,14]}";
        IExpressionContext ctx = Entry.buildContext(json);
        Entry.buildExpressionBuilder("x = $.x").build().evaluate(ctx);
        Entry.buildExpressionBuilder("y = 11").build().evaluate(ctx);
        assertEquals(27, Entry.buildExpressionBuilder("sum($.y[@{_>12}])").build().evaluate(ctx).asLong());
        assertEquals(27, Entry.buildExpressionBuilder("sum($.y[@{_>$x}])").build().evaluate(ctx).asLong());
        assertEquals(39, Entry.buildExpressionBuilder("sum($.y[@{_>$y}])").build().evaluate(ctx).asLong());
        assertEquals(27, Entry.buildExpressionBuilder("sum($.y[@{_>(y=12)}])").build().evaluate(ctx).asLong());
        assertEquals(27, Entry.buildExpressionBuilder("sum(filter($.y, @{_>(y=$x)}))").build().evaluate(ctx).asLong());
        assertEquals(11, Entry.buildExpressionBuilder("y").build().evaluate(ctx).asLong());
    }

    @Test
    public void testCrossOperators() {
        assertEquals(Entry.buildExpressionBuilder("[2]*2").build().evaluate(), Entry.buildExpressionBuilder("[2]*2").build().evaluate());
        assertTrue(Entry.buildExpressionBuilder("[2]*2 == map([2],@{_*2})").build().evaluate().asVector()[0].asBoolean());
        assertTrue(Entry.buildExpressionBuilder("[2]*2 == 2*[2]").build().evaluate().asVector()[0].asBoolean());
    }

    @Test
    public void testChainedCall() {
        assertEquals(7, Entry.buildExpressionBuilder("filter([1,2,3,4,5], @{_>2}).take(2).sum()").build().evaluate().asLong());
        assertEquals(6, Entry.buildExpressionBuilder("[1,2,3,4,5,6,7].filter(@{_>2}).sort().take(4).max()").build().evaluate().asLong());
        assertEquals(9, Entry.buildExpressionBuilder("[1,2,3,4,5,6,7].take(2).sum() + [1,2,3,4,5,6,7].take(3).sum()").build().evaluate().asLong());
        assertEquals(3, Entry.buildExpressionBuilder("[1,2,3,4].take([1,2].sum())").build().evaluate().asVector().length);
        assertEquals(0, Entry.buildExpressionBuilder("([1,2,3,4]-[4,3,2,1]).sum()").build().evaluate().asLong());
        assertEquals("[-9, -8, -7, -6]", Entry.buildExpressionBuilder("[1,2,3,4]-[4,3,2,1].sum()").build().evaluate().toString());
    }

    @Test
    public void testRound() {
        assertEquals(1.23d, Entry.buildExpressionBuilder("round(1.234)").build().evaluate().asDouble(), 0d);
        assertEquals(1.24d, Entry.buildExpressionBuilder("round(1.235)").build().evaluate().asDouble(), 0d);
        assertEquals(1.23d, Entry.buildExpressionBuilder("round(1.234, 2)").build().evaluate().asDouble(), 0d);
        assertEquals(1.24d, Entry.buildExpressionBuilder("round(1.235, 2)").build().evaluate().asDouble(), 0d);
        assertEquals(1.23d, Entry.buildExpressionBuilder("round(1.234, 2, \"HALF_UP\")").build().evaluate().asDouble(), 0d);
        assertEquals(1.24d, Entry.buildExpressionBuilder("round(1.235, 2, \"HALF_UP\")").build().evaluate().asDouble(), 0d);
        assertEquals(1.23d, Entry.buildExpressionBuilder("round(1.235, 2, \"HALF_DOWN\")").build().evaluate().asDouble(), 0d);
        assertEquals(1.24d, Entry.buildExpressionBuilder("round(1.236, 2, \"HALF_DOWN\")").build().evaluate().asDouble(), 0d);
        assertEquals(1.24d, Entry.buildExpressionBuilder("round(1.235, 2, \"UP\")").build().evaluate().asDouble(), 0d);
        assertEquals(1.23d, Entry.buildExpressionBuilder("round(1.235, 2, \"DOWN\")").build().evaluate().asDouble(), 0d);
    }

    @Test(expected = EvaluationException.class)
    public void testRound2() {
        Entry.buildExpressionBuilder("round(1.235, -2, \"HALF_UP\")").build().evaluate();
    }

    @Test(expected = EvaluationException.class)
    public void testRound3() {
        Entry.buildExpressionBuilder("round(1.235, 2, \"HALF\")").build().evaluate();
    }

    @Test
    public void testFormatDate() {
        assertEquals("2024-04-01", Entry.buildExpressionBuilder("formatDate(toDateFmt(\"2024-04-01 10:22:25\", \"yyyy-MM-dd HH:mm:ss\"), \"yyyy-MM-dd\")").build().evaluate().asString());
        assertEquals("10:22:25", Entry.buildExpressionBuilder("formatDate(toDateFmt(\"2024-04-01 10:22:25\", \"yyyy-MM-dd HH:mm:ss\"), \"HH:mm:ss\")").build().evaluate().asString());
    }

    @Test(expected = EvaluationException.class)
    public void testFormatDateException() {
        Entry.buildExpressionBuilder("formatDate(toDateFmt(\"2024-04-01 10:22:25\", \"yyyy-MM-dd HH:mm:ss\"), \"XX\")").build().evaluate().asString();
    }

    @Test
    public void testSubExp() {
        IExpressionContext ctx = Entry.buildContext();
        Entry.buildExpressionBuilder("x=@{1+2;3*3}").build().evaluate(ctx);
        Value v = Entry.buildExpressionBuilder("x(1)").build().evaluate(ctx); // FIXME: require at least one argument
        assertEquals(9, v.asLong());
    }

    @Test
    public void testBetweenDate() {
        assertEquals(9, Entry.buildExpressionBuilder("betweenDate(toDate(\"2024-05-10\"),toDate(\"2024-05-01\"))").build().evaluate().asLong());
        assertEquals(1, Entry.buildExpressionBuilder("betweenDate(toDate(\"2024-06-10\"),toDate(\"2024-05-01\"),\"MONTHS\")").build().evaluate().asLong());
        assertEquals(3, Entry.buildExpressionBuilder("betweenDate(toDate(\"2027-06-10\"),toDate(\"2024-05-01\"),\"YEARS\")").build().evaluate().asLong());
    }

    @Test
    public void testReplaceAll() {
        assertEquals("123456", Entry.buildExpressionBuilder("replaceAll(\"12&3&456\",(\"&\"),\"\")").build().evaluate().asString());
        assertEquals("&&", Entry.buildExpressionBuilder("replaceAll(\"12&3&456\",(\"\\\\d\"),\"\")").build().evaluate().asString());
    }

}
