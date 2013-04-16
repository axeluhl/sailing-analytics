package com.sap.sailing.gwt.ui.client;

import java.util.LinkedHashSet;
import java.util.Set;

import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CompetitorsFilterSets {
    private Set<FilterSet<CompetitorDTO>> filterSets;
    
    private FilterSet<CompetitorDTO> activeFilterSet;

    public CompetitorsFilterSets() {
        filterSets = new LinkedHashSet<FilterSet<CompetitorDTO>>();
        activeFilterSet = null;
    }
    
    public FilterSet<CompetitorDTO> getActiveFilterSet() {
        return activeFilterSet;
    }

    public void setActiveFilter(FilterSet<CompetitorDTO> newActiveFilterSet) {
        if(newActiveFilterSet != null) {
            if(filterSets.contains(newActiveFilterSet)) {
                this.activeFilterSet = newActiveFilterSet;
            }
        } else {
            this.activeFilterSet = null;
        }
    }

    public boolean addFilterSet(FilterSet<CompetitorDTO> filterSet) {
        return filterSets.add(filterSet);
    }

    public boolean removeFilterSet(FilterSet<CompetitorDTO> filterSet) {
        if(filterSet == activeFilterSet) {
            activeFilterSet = null;
        }
        
        return filterSets.remove(filterSet);
    }
    
    public Set<FilterSet<CompetitorDTO>> getFilterSets() {
        return filterSets;
    }
}
