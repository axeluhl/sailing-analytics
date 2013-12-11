package com.sap.sailing.selenium.pages.gwt.query.expr;

import com.sap.sailing.selenium.pages.gwt.query.Expression;

public abstract class ComparableExpression<T extends Comparable<?>> extends SimpleExpression<T> {

    public ComparableExpression(Expression<? extends T> mixin) {
        super(mixin);
    }

}
