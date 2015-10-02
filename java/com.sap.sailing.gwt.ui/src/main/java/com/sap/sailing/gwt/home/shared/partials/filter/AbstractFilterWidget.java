package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sse.common.filter.Filter;

public abstract class AbstractFilterWidget<T> extends Composite implements FilterWidget<T> {
    
    private final List<FilterValueChangeHandler<T>> valueChangeHandlers = new ArrayList<>();

    @Override
    public HandlerRegistration addFilterValueChangeHandler(final FilterValueChangeHandler<T> handler) {
        valueChangeHandlers.add(handler);
        handler.onFilterValueChanged(getFilter());
        return new HandlerRegistration() {
            @Override
            public void removeHandler() {
                valueChangeHandlers.remove(handler);
            }
        };
    }
    
    protected void notifyValueChangeHandlers() {
        Filter<T> filter = getFilter();
        for(FilterValueChangeHandler<T> valueChangeHandler : valueChangeHandlers) {
            valueChangeHandler.onFilterValueChanged(filter);
        }
    }

}
