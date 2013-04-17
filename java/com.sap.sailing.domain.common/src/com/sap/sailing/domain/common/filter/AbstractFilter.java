package com.sap.sailing.domain.common.filter;

import com.sap.sailing.domain.common.impl.Util.Pair;

public abstract class AbstractFilter<FilterObjectType, ValueType> implements Filter<FilterObjectType, ValueType> {
    protected ValueType filterValue;
    protected FilterOperators filterOperator;
    
    public AbstractFilter() {
        filterValue = null;
        filterOperator = null;
    }
    
    @Override
    public Pair<FilterOperators, ValueType> getConfiguration() {
        return new Pair<FilterOperators, ValueType>(filterOperator, filterValue);
    }

    @Override
    public void setConfiguration(Pair<FilterOperators, ValueType> filterValueAndOperator) {
        filterOperator = filterValueAndOperator.getA();
        filterValue = filterValueAndOperator.getB();
    }
}
