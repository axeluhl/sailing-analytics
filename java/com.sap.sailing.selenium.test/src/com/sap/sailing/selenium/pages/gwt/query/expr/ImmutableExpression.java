package com.sap.sailing.selenium.pages.gwt.query.expr;

import com.sap.sailing.selenium.pages.gwt.query.Expression;

/**
 * <p>ImmutableExpression is the base class for immutable Expression implementations.</p>
 * 
 * @author
 *   D049941
 * @param <T>
 *   The type the expression is bound to.
 */
public abstract class ImmutableExpression<T> implements Expression<T> {
    private final Class<? extends T> type;
    
    public ImmutableExpression(Class<? extends T> type) {
        this.type = type;
    }
    
    @Override
    public final Class<? extends T> getType() {
        return this.type;
    }
}
