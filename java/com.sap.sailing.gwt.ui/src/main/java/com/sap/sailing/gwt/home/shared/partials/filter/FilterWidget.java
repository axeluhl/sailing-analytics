package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.Collection;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sse.common.filter.Filter;

public interface FilterWidget<T, C> extends IsWidget {

    HandlerRegistration addFilterValueChangeHandler(FilterValueChangeHandler<T, C> handler);
    
    Filter<T> getFilter();
    
    void setSelectableValues(Collection<C> selectableValues);
}
