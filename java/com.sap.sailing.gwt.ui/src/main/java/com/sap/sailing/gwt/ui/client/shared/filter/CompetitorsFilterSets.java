package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.LinkedHashSet;
import java.util.Set;

import com.sap.sailing.domain.common.filter.FilterSet;
import com.sap.sailing.gwt.ui.client.ValueFilterWithUI;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CompetitorsFilterSets {
    private Set<FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>>> filterSets;
    
    private FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>> activeFilterSet;

    public CompetitorsFilterSets() {
        filterSets = new LinkedHashSet<FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>>>();
        activeFilterSet = null;
    }
    
    public FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>> getActiveFilterSet() {
        return activeFilterSet;
    }

    public void setActiveFilterSet(FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>> newActiveFilterSet) {
        if(newActiveFilterSet != null) {
            if(filterSets.contains(newActiveFilterSet)) {
                this.activeFilterSet = newActiveFilterSet;
            }
        } else {
            this.activeFilterSet = null;
        }
    }

    public boolean addFilterSet(FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>> filterSet) {
        return filterSets.add(filterSet);
    }

    public boolean removeFilterSet(FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>> filterSet) {
        if(filterSet == activeFilterSet) {
            activeFilterSet = null;
        }
        
        return filterSets.remove(filterSet);
    }
    
    public Set<FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>>> getFilterSets() {
        return filterSets;
    }
}
