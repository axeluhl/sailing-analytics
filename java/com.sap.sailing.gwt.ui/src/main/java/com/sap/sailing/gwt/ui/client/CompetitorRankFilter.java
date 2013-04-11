package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CompetitorRankFilter extends AbstractFilter<CompetitorDTO, Integer> implements CollectionFilter<CompetitorDTO, Integer> {
    private static List<FilterOperators> supportedOperators;
    
    static {
        supportedOperators = new ArrayList<FilterOperators>();
        supportedOperators.add(FilterOperators.GreaterThan);
        supportedOperators.add(FilterOperators.LessThan);
    }
    
    public Class<Integer> getValueType() {
        return Integer.class;
    }

    public CompetitorRankFilter() {
        filterValue = -1;
    }

    @Override
    public Collection<CompetitorDTO> filter(Collection<CompetitorDTO> competitors) {
        Set<CompetitorDTO> result = new LinkedHashSet<CompetitorDTO>();
        if(filterValue > 0) {
            int counter = 1;
            for(CompetitorDTO competitor :competitors) {
                if(counter > filterValue) {
                    break;
                }
                result.add(competitor);
                counter++;
            }
        } else {
            result.addAll(competitors);
        }
        
        return result;
    }

    @Override
    public String getName() {
        return "Top X competitors";
    }
    
    @Override
    public String getDescription() {
        return "Top X competitors";
    }

    @Override
    public Iterable<FilterOperators> getSupportedOperators() {
        return supportedOperators;
    }

    @Override
    public Filter<CompetitorDTO, Integer> copy() {
        CompetitorRankFilter result = new CompetitorRankFilter();
        result.setConfiguration(getConfiguration());
        return result; 
    }
}
