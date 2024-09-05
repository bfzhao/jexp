package org.my;

public interface IExpressionContext {
    void updateVariable(String name, Object value);

    Value getVariable(String name);

    IExpressionContext makeCopy();
}
