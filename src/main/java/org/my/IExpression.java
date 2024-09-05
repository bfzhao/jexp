package org.my;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public interface IExpression {
    Future<Value> evaluateAsync(ExecutorService executor, IExpressionContext context);

    Value evaluate(IExpressionContext context);

    Value evaluate();
}
