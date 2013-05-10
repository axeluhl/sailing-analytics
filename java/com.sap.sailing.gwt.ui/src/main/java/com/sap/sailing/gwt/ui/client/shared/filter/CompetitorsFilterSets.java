package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.LinkedHashSet;
import java.util.Set;

import com.sap.sailing.domain.common.filter.FilterSet;
import com.sap.sailing.gwt.ui.client.FilterWithUI;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CompetitorsFilterSets {
    private Set<FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>> filterSets;
    
    private FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> activeFilterSet;

    public CompetitorsFilterSets() {
        filterSets = new LinkedHashSet<FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>>();
        activeFilterSet = null;
    }
    
    public FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> getActiveFilterSet() {
        return activeFilterSet;
    }

    public FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> findFilterSetByName(String filterSetName) {
        FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> result = null;
        for(FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> filterSet: filterSets) {
            if(filterSet.getName().equals(filterSetName)) {
                result = filterSet;
                break;
            }
        }
        return result;
    }
    
    public void setActiveFilterSet(FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> newActiveFilterSet) {
        if(newActiveFilterSet != null) {
            if(filterSets.contains(newActiveFilterSet)) {
                this.activeFilterSet = newActiveFilterSet;
            }
        } else {
            this.activeFilterSet = null;
        }
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
    
    public Set<FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>> getFilterSets() {
        return filterSets;
    }
}
