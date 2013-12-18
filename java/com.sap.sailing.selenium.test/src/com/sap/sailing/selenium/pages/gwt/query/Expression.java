package com.sap.sailing.selenium.pages.gwt.query;

/**
 * <p>Expression defines a general typed expression in a query instance. The generic type parameter is a reference to
 *   the type the expression is bound to.</p>
 * 
 * @author D049941
 *
 * @param <T>
 *   The type the expression is bound to.
 */
public interface Expression<T> {
    /**
     * <p>Returns the type the expression is bound to.</p>
     * 
     * @return
     *   he type the expression is bound to.
     */
    public Class<? extends T> getType();
    
    public T evaluate(Object argument);
}
