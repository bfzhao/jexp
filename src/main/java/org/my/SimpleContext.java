package org.my;

import org.json.simple.parser.JSONParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleContext implements IExpressionContext {
    private final Map<String, Value> variables = new ConcurrentHashMap<>();
    public static final String CUR_VAR_NAME = "_";

    private void fillDefaultVariables() {
        variables.put("pi", Value.of(Math.PI));
        variables.put("e", Value.of(Math.E));
    }

    public SimpleContext() {
        this.fillDefaultVariables();
    }

    public SimpleContext(SimpleContext ctx) {
        this.variables.putAll(ctx.variables);
    }

    public SimpleContext(String json) {
        try {
            this.fillDefaultVariables();
            this.variables.put(CUR_VAR_NAME, Value.of(new JSONParser().parse(json)));
        } catch (Exception e) {
            throw new RuntimeException("fail to initialize context: " + e.getMessage());
        }
    }

    @Override
    public void updateVariable(String name, Object value) {
        variables.put(name, Value.of(value));
    }

    @Override
    public Value getVariable(String name) {
        return variables.getOrDefault(name, Value.NULL);
    }

    @Override
    public IExpressionContext makeCopy() {
        return new SimpleContext(this);
    }
}
