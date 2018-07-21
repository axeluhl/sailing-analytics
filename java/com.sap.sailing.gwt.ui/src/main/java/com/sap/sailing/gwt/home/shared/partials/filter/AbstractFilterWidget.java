package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sse.common.filter.Filter;

/**
 * Abstract implementation of {@link FilterWidget} holding a list of
 * {@link #addFilterValueChangeHandler(FilterValueChangeHandler) added} {@link FilterValueChangeHandler handlers} and
 * providing a {@link #notifyValueChangeHandlers() convenience method} to notify them.
 * 
 * @param <T>
 *            the actual type of values to be filtered
 * @param <C>
 *            the actual type of values to select
 */
public abstract class AbstractFilterWidget<T, C> extends Composite implements FilterWidget<T, C> {

    private final List<FilterValueChangeHandler<T>> valueChangeHandlers = new ArrayList<>();

    @Override
    public HandlerRegistration addFilterValueChangeHandler(final FilterValueChangeHandler<T> handler) {
        valueChangeHandlers.add(handler);
        return () -> valueChangeHandlers.remove(handler);
    }

    /**
     * Convenience method to notify all {@link #addFilterValueChangeHandler(FilterValueChangeHandler) added}
     * {@link FilterValueChangeHandler handler}s.
     */
    protected void notifyValueChangeHandlers() {
        Filter<T> filter = getFilter();
        valueChangeHandlers.forEach(valueChangeHandler -> valueChangeHandler.onFilterValueChanged(filter));
    }

}
