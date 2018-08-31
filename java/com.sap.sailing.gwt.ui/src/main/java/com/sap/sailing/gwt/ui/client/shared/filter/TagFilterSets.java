package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

public class TagFilterSets {
    private List<FilterSet<TagDTO, FilterWithUI<TagDTO>>> filterSets;

    private FilterSet<TagDTO, FilterWithUI<TagDTO>> activeFilterSet;

    public TagFilterSets() {
        filterSets = new ArrayList<FilterSet<TagDTO, FilterWithUI<TagDTO>>>();
        activeFilterSet = null;
    }

    public FilterSet<TagDTO, Filter<TagDTO>> getActiveFilterSetWithGeneralizedType() {
        FilterSet<TagDTO, Filter<TagDTO>> result = null;
        if (activeFilterSet != null) {
            result = new FilterSet<TagDTO, Filter<TagDTO>>(activeFilterSet.getName());
            for (Filter<TagDTO> filter : activeFilterSet.getFilters()) {
                result.addFilter(filter);
            }
        }
        return result;
    }

    public FilterSet<TagDTO, FilterWithUI<TagDTO>> getActiveFilterSet() {
        return activeFilterSet;
    }

    public FilterSet<TagDTO, FilterWithUI<TagDTO>> findFilterSetByName(String filterSetName) {
        FilterSet<TagDTO, FilterWithUI<TagDTO>> result = null;
        for (FilterSet<TagDTO, FilterWithUI<TagDTO>> filterSet : filterSets) {
            if (filterSet.getName().equals(filterSetName)) {
                result = filterSet;
                break;
            }
        }
        return result;
    }

    public void setActiveFilterSet(FilterSet<TagDTO, FilterWithUI<TagDTO>> newActiveFilterSet) {
        if (newActiveFilterSet != null) {
            if (filterSets.contains(newActiveFilterSet)) {
                this.activeFilterSet = newActiveFilterSet;
            }
        } else {
            this.activeFilterSet = null;
        }
    }

    public void addFilterSet(int position, FilterSet<TagDTO, FilterWithUI<TagDTO>> filterSet) {
        filterSets.add(position, filterSet);
    }

    public boolean addFilterSet(FilterSet<TagDTO, FilterWithUI<TagDTO>> filterSet) {
        return filterSets.add(filterSet);
    }

    public boolean removeFilterSet(FilterSet<TagDTO, FilterWithUI<TagDTO>> filterSet) {
        if (filterSet == activeFilterSet) {
            activeFilterSet = null;
        }
        return filterSets.remove(filterSet);
    }

    public List<FilterSet<TagDTO, FilterWithUI<TagDTO>>> getFilterSets() {
        return filterSets;
    }
    
    public void removeAllFilterSets() {
        filterSets.clear();
    }
}
