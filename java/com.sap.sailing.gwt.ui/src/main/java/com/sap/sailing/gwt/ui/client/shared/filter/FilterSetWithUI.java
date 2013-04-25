package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.Set;

import com.sap.sailing.domain.common.filter.FilterSet;
import com.sap.sailing.gwt.ui.client.FilterWithUI;

public class FilterSetWithUI<FilterObjectType> extends FilterSet<FilterObjectType> {

    public FilterSetWithUI(String name) {
        super(name);
    }

    public boolean addFilter(FilterWithUI<FilterObjectType, ?> filter) {
        return super.addFilter(filter);
    }

    public boolean removeFilter(FilterWithUI<FilterObjectType, ?> filter) {
        return super.removeFilter(filter);
    }

    @SuppressWarnings("unchecked")
    public Set<FilterWithUI<FilterObjectType, ?>> getFilters() {
        return (Set<FilterWithUI<FilterObjectType, ?>>) super.getFilters();
    }
}
