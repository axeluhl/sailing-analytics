package com.sap.sailing.gwt.ui.client;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A named set of filters.
 * Each of the filters has it's own configuration.
 * The class supports both kind of filters: {@link ObjectFilter}s and {@link CollectionFilter}s
 * @author Frank
 *
 */
public class FilterSet<FilterObjectType> {
    /** the name of the filter set */
    private String name;

    /** the set of all filters */
    private final Set<Filter<FilterObjectType, ?>> filters;

    public FilterSet(String name) {
        this.name = name;
        this.filters = new LinkedHashSet<Filter<FilterObjectType, ?>>();
    }

    public boolean addFilter(Filter<FilterObjectType, ?> filter) {
        return filters.add(filter);
    }

    public boolean removeFilter(Filter<FilterObjectType, ?> filter) {
        return filters.remove(filter);
    }

    public Set<Filter<FilterObjectType, ?>> getFilters() {
        return filters;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
