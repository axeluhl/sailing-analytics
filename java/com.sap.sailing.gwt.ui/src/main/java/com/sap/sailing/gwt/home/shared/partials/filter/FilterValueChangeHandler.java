package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.Collection;

import com.sap.sse.common.filter.Filter;

public interface FilterValueChangeHandler<T, C> {
    
    void onFilterValueChanged(Filter<T> filter);
    
    Collection<C> getFilterableValues();

}
