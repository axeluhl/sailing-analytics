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
        supportedOperators.add(FilterOperators.Equals);
    }
    
    public Class<Integer> getValueType() {
        return Integer.class;
    }

    public CompetitorRankFilter() {
        super();
    }


    @Override
    public FilterOperators getDefaultOperator() {
        return FilterOperators.GreaterThanEquals;
    }
    
    @Override
    public Collection<CompetitorDTO> filter(Collection<CompetitorDTO> competitors) {
        Set<CompetitorDTO> result = new LinkedHashSet<CompetitorDTO>();
        if(filterValue > 0 && filterOperator != null) {
            int counter = 1;
            for(CompetitorDTO competitor :competitors) {
                switch (filterOperator) {
                    case Equals:
                        if(counter > filterValue) {
                            return result;
                        }
                        break;
                    case GreaterThanEquals:
                    case LessThan:
                    case LessThanEquals:
                    case Contains:
                    case EndsWith:
                    case GreaterThan:
                    case NotContains:
                    case NotEqualTo:
                    case StartsWith:
                        throw new RuntimeException("Operator " + filterOperator.name() + " is not supported."); 
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
        return "Top ranked competitors";
    }
    
    @Override
    public String getDescription() {
        return "Shows the top [number] ranked competitors";
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
