package com.sap.sailing.domain.common.filter;

public interface FilterOperator<ValueType> {
    boolean matchValues(ValueType filterValue, ValueType valueToMatch);
    
    String getName();
}
