package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.Collection;

/**
 * Handler interface for class provide the values available for selection in a {@link FilterWidget}.
 * 
 * @param <C>
 *            the actual type of values to select
 */
@FunctionalInterface
public interface FilterValueProvider<C> {

    /**
     * Provides the values available for selection in a {@link FilterWidget widget}.
     * 
     * @return {@link Collection} of select-able values
     */
    Collection<C> getFilterableValues();

}
