package com.sap.sse.common.filter;

public interface FilterOperator<ValueType> {
    boolean matchValues(ValueType filterValue, ValueType valueToMatch);
    
    String getName();
}
