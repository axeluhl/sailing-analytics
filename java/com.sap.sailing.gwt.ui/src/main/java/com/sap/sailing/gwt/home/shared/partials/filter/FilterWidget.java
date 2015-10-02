package com.sap.sailing.gwt.home.shared.partials.filter;

import com.google.gwt.event.shared.HandlerRegistration;
import com.sap.sse.common.filter.Filter;

public interface FilterWidget<T> {

    HandlerRegistration addFilterValueChangeHandler(FilterValueChangeHandler<T> handler);
    
    Filter<T> getFilter();
}
