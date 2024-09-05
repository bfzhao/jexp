package org.my;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class ValueTest {
    @Test
    public void testAsRawObject() {
        Value v;
        assertEquals(12L, Value.of(12).asRawObject());
        assertEquals(12.4, Value.of(12.4).asRawObject());
        assertEquals(false, Value.FALSE.asRawObject());
        assertEquals("hello", Value.of("hello").asRawObject());

        Object[] list = (Object[]) Value.of(new Value[]{Value.of(12), Value.FALSE}).asRawObject();
        assertEquals(12L, list[0]);
        assertEquals(false, list[1]);

        Map<String, Value> map = new HashMap<>();
        map.put("x", Value.of(42));
        map.put("y", Value.of("hello"));
        v = Value.of(map);

        Map<?, ?> mo = (Map<?, ?>) v.asRawObject();
        assertEquals(42L, mo.get("x"));
        assertEquals("hello", mo.get("y"));
    }

    @Test
    public void testValueEqual() {
        assertEquals(Value.NULL, Value.NULL);
        assertEquals(Value.of("abc"), Value.of("abc"));
        assertEquals(Value.FALSE, Value.FALSE);
        assertEquals(Value.of(12), Value.of(12));
        assertEquals(Value.of(new Value[]{Value.NULL, Value.of("12")}),
                Value.of(new Value[]{Value.NULL, Value.of("12")}));
        assertEquals(Value.of(new HashMap<String, Value>(){{put("my", Value.of("12"));}}),
                Value.of(new HashMap<String, Value>(){{put("my", Value.of("12"));}}));

        assertNotEquals(null, Value.of());
        assertNotEquals(Value.of(12), Value.of(12.0f));
        assertNotEquals(Value.of(12), null);
        assertNotEquals(Value.of(new HashMap<String, Value>(){{put("my", Value.of("12"));}}),
                Value.of(new HashMap<String, Value>(){{put("his", Value.of("12"));}}));
        assertNotEquals(Value.of(new Value[]{Value.NULL, Value.of("12")}),
                Value.of(new Value[]{Value.of("12"), Value.NULL}));
    }

    @Test
    public void testValueCompare() {
        assertEquals(0, Value.NULL.compareTo(Value.of()));
    }

    @Test(expected = Exp4jException.EvaluationException.class)
    public void testValueCompareBad() {
        assertEquals(0, Value.of(12).compareTo(Value.of("String")));
    }

    @Test(expected = Exp4jException.EvaluationException.class)
    public void testValueCompareBad2() {
        assertEquals(0, Value.of(new Value[]{}).compareTo(Value.of(new Value[]{})));
    }

    @Test(expected = Exp4jException.EvaluationException.class)
    public void testVectorValue() {
        Value v = Value.of(12);
        assertFalse(v.isExpression());
        v.asVector();
    }

    @Test(expected = Exp4jException.EvaluationException.class)
    public void testBadValue() {
        Value.of(12).asMap();
    }

    @Test(expected = Exp4jException.EvaluationException.class)
    public void testBadValue2() {
        Value.of(12).asExpression();
    }

    @Test
    public void testEnclosedValue() {
        assertTrue(Value.enclosed(new Value[]{Value.of(12)}).isNumber());
        assertTrue(Value.enclosed(new Value[]{Value.enclosed(new Value[]{Value.of(12)})}).isNumber());
    }

    @Test(expected = Exp4jException.EvaluationException.class)
    public void testValueOf() {
        Value.of(new Object());
    }

    @Test(expected = Exp4jException.EvaluationException.class)
    public void testValueAs() {
        Value.of("new Object()").asDateTime();
    }

    @Test
    public void testUnwrap() {
        assertEquals("12", Entry.buildExpressionBuilder("(12)").build().evaluate().toString());
        assertEquals("-12", Entry.buildExpressionBuilder("-((12))").build().evaluate().toString());
        assertEquals("[12]", Entry.buildExpressionBuilder("([12])").build().evaluate().toString());
        assertEquals("[12]", Entry.buildExpressionBuilder("(([12]))").build().evaluate().toString());
    }
}
