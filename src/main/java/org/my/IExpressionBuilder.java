package org.my;

import java.util.List;

public interface IExpressionBuilder {
    IExpression build();

    List<IExpression> buildAll();
}
