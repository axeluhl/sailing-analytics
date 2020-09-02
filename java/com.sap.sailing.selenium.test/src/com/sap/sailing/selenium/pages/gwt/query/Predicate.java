package com.sap.sailing.selenium.pages.gwt.query;

/**
 * <p>Predicate is the common interface for Boolean typed expressions.</p>
 * 
 * @author D049941
 */
public interface Predicate extends Expression<Boolean> {
    /**
     * <p>Returns the negation of the expression.</p>
     * 
     * @return
     *   The negation of the expression.
     */
    public Predicate not();
}
