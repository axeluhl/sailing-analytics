package com.sap.sailing.selenium.pages.gwt.query.expr;

import com.sap.sailing.selenium.pages.gwt.query.Expression;

public class NumberExpression<T extends Number & Comparable<?>> extends ComparableExpression<T> {

    public NumberExpression(Expression<? extends T> mixin) {
        super(mixin);
    }

}
