package org.my;

import org.json.simple.parser.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class ExceptionTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test1() throws org.json.simple.parser.ParseException {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected 'a' at pos 7");
        IExpressionContext context = Entry.buildContext("{\"arr\":[1,2,3]}");
        Entry.buildExpressionBuilder("$.arr[a]").build().evaluate(context);
    }

    @Test
    public void test2() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected '(' at pos 7");
        Entry.buildExpressionBuilder("sum([])(").build().evaluate();
    }

    @Test
    public void test3_1() {
        thrown.expect(Exp4jException.EvaluationException.class);
        thrown.expectMessage("invalid argument count");
        Entry.buildExpressionBuilder("union([])").build().evaluate();
    }

    @Test
    public void test3_2() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected EOF");
        Entry.buildExpressionBuilder("1+").build().evaluate();
    }

    @Test
    public void test4() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected EOF");
        Entry.buildExpressionBuilder("\"123").build().evaluate();
    }

    @Test
    public void test5_1() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected EOF");
        Entry.buildExpressionBuilder("$.x[12").build().evaluate();
    }

    @Test
    public void test5_2() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected EOF");
        Entry.buildExpressionBuilder("$.\"xx").build().evaluate();
    }

    @Test
    public void test5_3() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected EOF");
        Entry.buildExpressionBuilder("$.x[").build().evaluate();
    }

    @Test
    public void test6() throws ParseException {
        IExpressionContext ctx = Entry.buildContext("{\"\":2}");
        Value v = Entry.buildExpressionBuilder("$.\"\"").build().evaluate(ctx);
        assertEquals(2, v.asLong());
    }

    @Test
    public void test7_1() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected '+' at pos 5");
        Entry.buildExpressionBuilder("$.x. + 2").build().evaluate();
    }

    @Test
    public void test7_2() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected '}' at pos 2");
        Entry.buildExpressionBuilder("${}").build().evaluate();
    }

    @Test
    public void test7_3() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected '+' at pos 2");
        Entry.buildExpressionBuilder("$ + 2").build().evaluate();
    }

    @Test
    public void test8() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected EOF");
        Entry.buildExpressionBuilder("@{ x = 12").build().evaluate();
    }

    @Test
    public void test9() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected '\\s' at pos 6");
        Entry.buildExpressionBuilder("\"123\\s").build().evaluate();
    }

    @Test
    public void test10() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected 'e' at pos 3");
        Entry.buildExpressionBuilder("12e").build().evaluate();
    }

    @Test
    public void test11() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected '~' at pos 3");
        Entry.buildExpressionBuilder("123~").build().evaluate();
    }

    @Test
    public void testLeftUnmatchParenthesis() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected ',' at pos 2");
        IExpression e = Entry.buildExpressionBuilder("12, 23]").build();
        e.evaluate();
    }

    @Test
    public void testPseudoNode1() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected ':' at pos 1");
        IExpression e = Entry.buildExpressionBuilder("[:12]").build();
        e.evaluate();
    }

    @Test
    public void testPseudoNode2() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected ',' at pos 1");
        IExpression e = Entry.buildExpressionBuilder("{,12}").build();
        e.evaluate();
    }

    @Test
    public void testPseudoNode3() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected ':' at pos 1");
        IExpression e = Entry.buildExpressionBuilder("{:12:12}").build();
        e.evaluate();
    }

    @Test
    public void testPseudoNode4() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected ',' at pos 1");
        IExpression e = Entry.buildExpressionBuilder("{,}").build();
        e.evaluate();
    }

    @Test
    public void testPseudoNode5() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected ':' at pos 1");
        IExpression e = Entry.buildExpressionBuilder("{:}").build();
        e.evaluate();
    }

    @Test
    public void testPseudoNode6() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected '14' at pos 4");
        IExpression e = Entry.buildExpressionBuilder("[12 14]").build();
        e.evaluate();
    }

    @Test
    public void testPseudoNode7() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected '12' at pos 1");
        IExpression e = Entry.buildExpressionBuilder("{12 14}").build();
        e.evaluate();
    }

    @Test
    public void testPseudoNode8() {
        thrown.expect(Exp4jException.ParseException.class);
        thrown.expectMessage("unexpected '12' at pos 1");
        IExpression e = Entry.buildExpressionBuilder("{12 13:14}").build();
        e.evaluate();
    }

    @Test
    public void testEnclosedNode() {
        IExpression e = Entry.buildExpressionBuilder("(1,2)").build();
        assertEquals(2, e.evaluate().asLong());
    }
}
