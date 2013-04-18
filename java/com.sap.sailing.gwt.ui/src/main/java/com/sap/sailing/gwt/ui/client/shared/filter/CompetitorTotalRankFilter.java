package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.filter.Filter;
import com.sap.sailing.domain.common.filter.FilterOperators;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CompetitorTotalRankFilter extends AbstractCompetitorInLeaderboardFilter<Integer> { 
    private static List<FilterOperators> supportedOperators;
    
    static {
        supportedOperators = new ArrayList<FilterOperators>();
        supportedOperators.add(FilterOperators.LessThanEquals);
    }
    
    public Class<Integer> getValueType() {
        return Integer.class;
    }

    public CompetitorTotalRankFilter() {
        super();
    }

    @Override
    public FilterOperators getDefaultOperator() {
        return FilterOperators.GreaterThanEquals;
    }
    
    @Override
    public boolean matches(CompetitorDTO competitorDTO) {
        boolean result = false;
        
        if(filterValue > 0 && filterOperator != null && getLeaderboard() != null) {
            int totalRank = getLeaderboard().getRank(competitorDTO);
            switch (filterOperator) {
                case LessThanEquals:
                    if(totalRank <= filterValue) {
                        result = true;
                    }
                    break;
                case Equals:
                case GreaterThanEquals:
                case LessThan:
                case Contains:
                case EndsWith:
                case GreaterThan:
                case NotContains:
                case NotEqualTo:
                case StartsWith:
                    throw new RuntimeException("Operator " + filterOperator.name() + " is not supported."); 
            }
        }
        
        return result;
    }
        
     @Override
    public String getName() {
        return "Total rank";
    }

    @Override
    public Iterable<FilterOperators> getSupportedOperators() {
        return supportedOperators;
    }

    @Override
    public Filter<CompetitorDTO, Integer> copy() {
        CompetitorTotalRankFilter result = new CompetitorTotalRankFilter();
        result.setContextProvider(getContextProvider());
        result.setConfiguration(getConfiguration());
        return result; 
    }

}
