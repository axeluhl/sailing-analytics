package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CompetitorNationalityFilter extends AbstractFilter<CompetitorDTO, String> implements ObjectFilter<CompetitorDTO, String> {
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
    public boolean filter(CompetitorDTO competitor) {
        boolean result = false;
        if(filterValue != null && filterValue.length() == 2) {
            if(competitor.twoLetterIsoCountryCode != null && competitor.twoLetterIsoCountryCode.equals(filterValue)) {
                result = true;
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
    public String getDescription() {
        return "Nationality";
    }

    @Override
    public Filter<CompetitorDTO, String> copy() {
        CompetitorNationalityFilter result = new CompetitorNationalityFilter();
        result.setConfiguration(getConfiguration());
        return result; 
    }

}
