package com.sap.sailing.domain.common.filter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A named set of filters.
 * @author Frank
 *
 */
public class FilterSet<FilterObjectType, T extends Filter<FilterObjectType>> {
    /** the name of the filter set */
    private String name;

    /** the set of all filters */
    private final Set<T> filters;

    public FilterSet(String name) {
        this.name = name;
        this.filters = new LinkedHashSet<T>();
    }

    public boolean addFilter(T filter) {
        return filters.add(filter);
    }

    public boolean removeFilter(T filter) {
        return filters.remove(filter);
    }

    public Set<T> getFilters() {
        return filters;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
