package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

public class CompetitorsFilterSets {
    private List<FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>> filterSets;
    
    private FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> activeFilterSet;

    public CompetitorsFilterSets() {
        filterSets = new ArrayList<FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>>();
        activeFilterSet = null;
    }
    
    public FilterSet<CompetitorDTO, Filter<CompetitorDTO>> getActiveFilterSetWithGeneralizedType() {
        FilterSet<CompetitorDTO, Filter<CompetitorDTO>> result = null;
        if(activeFilterSet != null) {
            result = new FilterSet<CompetitorDTO, Filter<CompetitorDTO>>(activeFilterSet.getName());
            for (Filter<CompetitorDTO> filter : activeFilterSet.getFilters()) {
                result.addFilter(filter);
            }
        }
        return result;
    }
    
    public FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> getActiveFilterSet() {
        return activeFilterSet;
    }

    public FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> findFilterSetByName(String filterSetName) {
        FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> result = null;
        for (FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> filterSet : filterSets) {
            if (filterSet.getName().equals(filterSetName)) {
                result = filterSet;
                break;
            }
        }
        return result;
    }
    
    public void setActiveFilterSet(FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> newActiveFilterSet) {
        if (newActiveFilterSet != null) {
            if (filterSets.contains(newActiveFilterSet)) {
                this.activeFilterSet = newActiveFilterSet;
            }
        } else {
            this.activeFilterSet = null;
        }
    }

    public void addFilterSet(int position, FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> filterSet) {
        filterSets.add(position, filterSet);
    }

    public boolean addFilterSet(FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> filterSet) {
        return filterSets.add(filterSet);
    }

    public boolean removeFilterSet(FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> filterSet) {
        if(filterSet == activeFilterSet) {
            activeFilterSet = null;
        }
        return filterSets.remove(filterSet);
    }
    
    public List<FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>> getFilterSets() {
        return filterSets;
    }
}
