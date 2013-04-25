package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.filter.FilterOperators;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CompetitorTotalRankFilter extends AbstractCompetitorInLeaderboardFilter<Integer> { 
    private static List<FilterOperators> supportedOperators;
    
    public static final String FILTER_NAME = "CompetitorTotalRankFilter";

    static {
        supportedOperators = new ArrayList<FilterOperators>();
        supportedOperators.add(FilterOperators.LessThanEquals);
        supportedOperators.add(FilterOperators.GreaterThanEquals);
        supportedOperators.add(FilterOperators.LessThan);
        supportedOperators.add(FilterOperators.GreaterThan);
        supportedOperators.add(FilterOperators.NotEqualTo);
        supportedOperators.add(FilterOperators.Equals);
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
        
        if (filterValue > 0 && filterOperator != null && getLeaderboard() != null) {
            int totalRank = getLeaderboard().getRank(competitorDTO);
            switch (filterOperator) {
                case LessThanEquals:
                    result = totalRank <= filterValue;
                    break;
                case Equals:
                    result = totalRank == filterValue;
                    break;
                case GreaterThanEquals:
                    result = totalRank >= filterValue;
                    break;
                case LessThan:
                    result = totalRank < filterValue;
                    break;
                case GreaterThan:
                    result = totalRank > filterValue;
                    break;
                case NotEqualTo:
                    result = totalRank != filterValue;
                    break;
                case NotContains:
                case StartsWith:
                case Contains:
                case EndsWith:
                default:
                    throw new RuntimeException("Operator " + filterOperator.name() + " is not supported."); 
            }
        }
        
        return result;
    }
        
     @Override
    public String getName() {
        return FILTER_NAME;
    }

    @Override
    public Iterable<FilterOperators> getSupportedOperators() {
        return supportedOperators;
    }
}
