package com.sap.sse.datamining.impl.criterias;

import com.sap.sse.datamining.components.FilterCriteria;

public abstract class RegexFilterCriteria<DataType> implements FilterCriteria<DataType> {
    
    private String regex;

    public RegexFilterCriteria(String regex) {
        this.regex = regex;
    }

    @Override
    public boolean matches(DataType data) {
        String valueToMatch = getValueToMatch(data);
        return valueToMatch != null && valueToMatch.matches(regex);
    }
    
    protected abstract String getValueToMatch(DataType data);

}
