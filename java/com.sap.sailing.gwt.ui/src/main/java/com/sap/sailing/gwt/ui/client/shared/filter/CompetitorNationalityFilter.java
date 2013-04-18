package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.filter.AbstractFilter;
import com.sap.sailing.domain.common.filter.Filter;
import com.sap.sailing.domain.common.filter.FilterOperators;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CompetitorNationalityFilter extends AbstractFilter<CompetitorDTO, String> implements Filter<CompetitorDTO, String> {
    private static List<FilterOperators> supportedOperators;
    
    static {
        supportedOperators = new ArrayList<FilterOperators>();
        supportedOperators.add(FilterOperators.Equals);
        supportedOperators.add(FilterOperators.NotEqualTo);
        supportedOperators.add(FilterOperators.Contains);
        supportedOperators.add(FilterOperators.NotContains);
    }
    
    public CompetitorNationalityFilter() {
        super();
    }

    @Override
    public FilterOperators getDefaultOperator() {
        return FilterOperators.Equals;
    }
    
    public Class<String> getValueType() {
        return String.class;
    }

    @Override
    public boolean matches(CompetitorDTO competitor) {
        boolean result = false;
        if(filterValue != null && filterOperator != null) {
            switch (filterOperator) {
            case Contains:
            case Equals:
                if(filterValue.length() == 2 && competitor.twoLetterIsoCountryCode != null && 
                    competitor.twoLetterIsoCountryCode.equalsIgnoreCase(filterValue)) {
                    result = true;
                } else if(filterValue.length() == 3 && competitor.threeLetterIocCountryCode != null && 
                        competitor.threeLetterIocCountryCode.equalsIgnoreCase(filterValue)) {
                    result = true;
                }
                break;
            case NotContains:
            case NotEqualTo:
                if(filterValue.length() == 2 && competitor.twoLetterIsoCountryCode != null && 
                    !competitor.twoLetterIsoCountryCode.equalsIgnoreCase(filterValue)) {
                    result = true;
                } else if(filterValue.length() == 3 && competitor.threeLetterIocCountryCode != null && 
                        !competitor.threeLetterIocCountryCode.equalsIgnoreCase(filterValue)) {
                    result = true;
                }
                break;
            case EndsWith:
            case GreaterThan:
            case GreaterThanEquals:
            case LessThan:
            case LessThanEquals:
            case StartsWith:
                throw new RuntimeException("Operator " + filterOperator.name() + " is not supported."); 
            }
        }
        return result;
    }

    @Override
    public Iterable<FilterOperators> getSupportedOperators() {
        return supportedOperators;
    }

    @Override
    public String getName() {
        return "Nationality";
    }

    @Override
    public Filter<CompetitorDTO, String> copy() {
        CompetitorNationalityFilter result = new CompetitorNationalityFilter();
        result.setConfiguration(getConfiguration());
        return result; 
    }

}
