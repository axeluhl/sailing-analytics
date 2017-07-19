package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.Collection;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sse.common.filter.Filter;

/**
 * Interface for {@link IsWidget widgets} used to filter a specific type values based on another type, where the values
 * available for selection can be {@link #setSelectableValues(Collection) set}.
 * 
 * @param <T>
 *            the actual type of values to be filtered
 * @param <C>
 *            the actual type of values to select
 */
public interface FilterWidget<T, C> extends IsWidget {

    /**
     * Adds a {@link FilterValueChangeHandler handler} to the {@link FilterWidget widget} which is called on values
     * changes.
     * 
     * @param handler
     *            the {@link FilterValueChangeHandler handler} to add
     * @return {@link HandlerRegistration} which can be used to remove the handler
     */
    HandlerRegistration addFilterValueChangeHandler(FilterValueChangeHandler<T> handler);

    /**
     * Provides a {@link Filter} instance based on the {@link FilterWidget widget} state.
     * 
     * @return {@link Filter} instance representing the {@link FilterWidget widget} state
     */
    Filter<T> getFilter();

    /**
     * Sets the values available for selection to the {@link FilterWidget widget}.
     * 
     * @param selectableValues
     *            {@link Collection} of select-able values
     */
    void setSelectableValues(Collection<C> selectableValues);
}
