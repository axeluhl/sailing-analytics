package com.sap.sailing.selenium.pages.gwt.query;

/**
 * <p>Constant represents a general constant expression.</p>
 * 
 * @author D049941
 *
 * @param <T>
 *   The type the constant is bound to.
 */
public interface Constant<T> extends Expression<T> {
    /**
     * <p>Returns the constant.</p>
     *
     * @return
     *   The constant.
     */
    public T getConstant();
}
