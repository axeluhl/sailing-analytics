package com.sap.sailing.domain.common.filter;



public interface ObjectFilter<FilterObjectType, ValueType> extends Filter<FilterObjectType, ValueType> {
    boolean filter(FilterObjectType object);
}

