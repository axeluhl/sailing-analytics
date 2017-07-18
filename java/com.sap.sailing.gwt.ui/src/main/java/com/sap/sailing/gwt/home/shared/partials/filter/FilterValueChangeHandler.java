package com.sap.sailing.gwt.home.shared.partials.filter;

import com.sap.sse.common.filter.Filter;

@FunctionalInterface
public interface FilterValueChangeHandler<T> {

    void onFilterValueChanged(Filter<T> filter);

}
