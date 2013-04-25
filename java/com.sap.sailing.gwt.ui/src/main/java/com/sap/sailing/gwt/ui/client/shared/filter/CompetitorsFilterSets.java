package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.LinkedHashSet;
import java.util.Set;

import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CompetitorsFilterSets {
    private Set<FilterSetWithUI<CompetitorDTO>> filterSets;
    
    private FilterSetWithUI<CompetitorDTO> activeFilterSet;

    public CompetitorsFilterSets() {
        filterSets = new LinkedHashSet<FilterSetWithUI<CompetitorDTO>>();
        activeFilterSet = null;
    }
    
    public FilterSetWithUI<CompetitorDTO> getActiveFilterSet() {
        return activeFilterSet;
    }

    public void setActiveFilterSet(FilterSetWithUI<CompetitorDTO> newActiveFilterSet) {
        if(newActiveFilterSet != null) {
            if(filterSets.contains(newActiveFilterSet)) {
                this.activeFilterSet = newActiveFilterSet;
            }
        } else {
            this.activeFilterSet = null;
        }
    }

    public boolean addFilterSet(FilterSetWithUI<CompetitorDTO> filterSet) {
        return filterSets.add(filterSet);
    }

    public boolean removeFilterSet(FilterSetWithUI<CompetitorDTO> filterSet) {
        if(filterSet == activeFilterSet) {
            activeFilterSet = null;
        }
        
        return filterSets.remove(filterSet);
    }
    
    public Set<FilterSetWithUI<CompetitorDTO>> getFilterSets() {
        return filterSets;
    }
}
