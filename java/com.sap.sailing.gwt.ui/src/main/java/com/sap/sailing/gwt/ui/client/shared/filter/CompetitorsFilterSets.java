package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

public class CompetitorsFilterSets {
    private List<FilterSet<CompetitorWithBoatDTO, FilterWithUI<CompetitorWithBoatDTO>>> filterSets;
    
    private FilterSet<CompetitorWithBoatDTO, FilterWithUI<CompetitorWithBoatDTO>> activeFilterSet;

    public CompetitorsFilterSets() {
        filterSets = new ArrayList<FilterSet<CompetitorWithBoatDTO, FilterWithUI<CompetitorWithBoatDTO>>>();
        activeFilterSet = null;
    }
    
    public FilterSet<CompetitorWithBoatDTO, Filter<CompetitorWithBoatDTO>> getActiveFilterSetWithGeneralizedType() {
        FilterSet<CompetitorWithBoatDTO, Filter<CompetitorWithBoatDTO>> result = null;
        if(activeFilterSet != null) {
            result = new FilterSet<CompetitorWithBoatDTO, Filter<CompetitorWithBoatDTO>>(activeFilterSet.getName());
            for (Filter<CompetitorWithBoatDTO> filter : activeFilterSet.getFilters()) {
                result.addFilter(filter);
            }
        }
        return result;
    }
    
    public FilterSet<CompetitorWithBoatDTO, FilterWithUI<CompetitorWithBoatDTO>> getActiveFilterSet() {
        return activeFilterSet;
    }

    public FilterSet<CompetitorWithBoatDTO, FilterWithUI<CompetitorWithBoatDTO>> findFilterSetByName(String filterSetName) {
        FilterSet<CompetitorWithBoatDTO, FilterWithUI<CompetitorWithBoatDTO>> result = null;
        for (FilterSet<CompetitorWithBoatDTO, FilterWithUI<CompetitorWithBoatDTO>> filterSet : filterSets) {
            if (filterSet.getName().equals(filterSetName)) {
                result = filterSet;
                break;
            }
        }
        return result;
    }
    
    public void setActiveFilterSet(FilterSet<CompetitorWithBoatDTO, FilterWithUI<CompetitorWithBoatDTO>> newActiveFilterSet) {
        if (newActiveFilterSet != null) {
            if (filterSets.contains(newActiveFilterSet)) {
                this.activeFilterSet = newActiveFilterSet;
            }
        } else {
            this.activeFilterSet = null;
        }
    }

    public void addFilterSet(int position, FilterSet<CompetitorWithBoatDTO, FilterWithUI<CompetitorWithBoatDTO>> filterSet) {
        filterSets.add(position, filterSet);
    }

    public boolean addFilterSet(FilterSet<CompetitorWithBoatDTO, FilterWithUI<CompetitorWithBoatDTO>> filterSet) {
        return filterSets.add(filterSet);
    }

    public boolean removeFilterSet(FilterSet<CompetitorWithBoatDTO, FilterWithUI<CompetitorWithBoatDTO>> filterSet) {
        if(filterSet == activeFilterSet) {
            activeFilterSet = null;
        }
        
        return filterSets.remove(filterSet);
    }
    
    public List<FilterSet<CompetitorWithBoatDTO, FilterWithUI<CompetitorWithBoatDTO>>> getFilterSets() {
        return filterSets;
    }
}
