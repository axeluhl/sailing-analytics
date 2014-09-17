package com.sap.sse.datamining.impl.criterias;

import com.sap.sse.datamining.components.FilterCriterion;

public abstract class RegexFilterCriterion<DataType> implements FilterCriterion<DataType> {
    
    private String regex;

    public RegexFilterCriterion(String regex) {
        this.regex = regex;
    }

    @Override
    public boolean matches(DataType data) {
        String valueToMatch = getValueToMatch(data);
        return valueToMatch != null && valueToMatch.matches(regex);
    }
    
    protected abstract String getValueToMatch(DataType data);

}
