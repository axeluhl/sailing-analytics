package com.sap.sailing.gwt.ui.client;


public interface ObjectFilter<FilterObjectType, ValueType> extends Filter<FilterObjectType, ValueType> {
    boolean filter(FilterObjectType object);
}

