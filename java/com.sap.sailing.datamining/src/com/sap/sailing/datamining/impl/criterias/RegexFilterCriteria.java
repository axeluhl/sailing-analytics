package com.sap.sailing.datamining.impl.criterias;

import com.sap.sailing.datamining.FilterCriteria;

public abstract class RegexFilterCriteria<DataType> implements FilterCriteria<DataType> {
    
    private String regex;

    public RegexFilterCriteria(String regex) {
        this.regex = regex;
    }

    @Override
    public boolean matches(DataType data) {
        return getValueToMatch(data).matches(regex);
    }
    
    protected abstract String getValueToMatch(DataType data);

}
