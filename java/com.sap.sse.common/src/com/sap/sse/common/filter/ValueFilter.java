package com.sap.sse.common.filter;

/**
 * A value filter using an operator to match a value to be filtered 
 * @author Frank
 */
public interface ValueFilter<FilterObjectType, ValueType> extends Filter<FilterObjectType> {
    ValueType getValue();
    
    FilterOperator<ValueType> getOperator();

    void setValue(ValueType val);
    
    void setOperator(FilterOperator<ValueType> operator);
}
