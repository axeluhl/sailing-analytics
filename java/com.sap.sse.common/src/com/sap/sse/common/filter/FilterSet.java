package com.sap.sse.common.filter;

import java.util.Collections;
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

    private boolean editable;
    
    public FilterSet(String name) {
        this.name = name;
        this.editable = true;
        this.filters = new LinkedHashSet<T>();
    }

    public boolean addFilter(T filter) {
        return filters.add(filter);
    }

    public boolean removeFilter(T filter) {
        return filters.remove(filter);
    }

    /**
     * The unmodifiable set of filters
     */
    public Set<T> getFilters() {
        return Collections.unmodifiableSet(filters);
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}
