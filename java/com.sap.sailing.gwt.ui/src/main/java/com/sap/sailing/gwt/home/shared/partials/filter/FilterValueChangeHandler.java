package com.sap.sailing.gwt.home.shared.partials.filter;

import com.sap.sse.common.filter.Filter;

/**
 * Handler interface for class which need to be informed about the value change of a {@link FilterWidget}.
 * 
 * @param <T>
 *            the actual type of values to be filtered
 */
@FunctionalInterface
public interface FilterValueChangeHandler<T> {

    /**
     * Called if the value of the {@link FilterWidget widget}, where this {@link FilterValueChangeHandler handler} was
     * added, changes providing the {@link Filter} representing the its state.
     * 
     * @param filter
     *            {@link Filter} instance representing the {@link FilterWidget widget} state
     */
    void onFilterValueChanged(Filter<T> filter);

}
