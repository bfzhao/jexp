package org.my;

import org.json.simple.parser.ParseException;

public class Entry {
    public static IExpressionContext buildContext() {
        return new SimpleContext();
    }

    public static IExpressionBuilder buildExpressionBuilder(String expression) {
        return new JExpParser.SimpleBuilder(expression);
    }

    public static IExpressionContext buildContext(String json) throws ParseException {
        return new SimpleContext(json);
    }
}
