package com.sap.sailing.gwt.home.shared.partials.filter;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sse.common.filter.Filter;

public interface FilterWidget<T> extends IsWidget {

    HandlerRegistration addFilterValueChangeHandler(FilterValueChangeHandler<T> handler);
    
    Filter<T> getFilter();
}
