package com.sap.sailing.datamining.impl.criterias;

import com.sap.sailing.datamining.ConcurrentFilterCriteria;

public abstract class RegexFilterCriteria<DataType> implements ConcurrentFilterCriteria<DataType> {
    
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
