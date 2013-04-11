package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.impl.Util.Pair;

public interface Filter<FilterObjectType, ValueType> {
    Iterable<FilterOperators> getSupportedOperators();

    Class<ValueType> getValueType();
    
    Pair<FilterOperators, ValueType> getConfiguration();

    void setConfiguration(Pair<FilterOperators, ValueType> filterValueAndOperator);

    String getName();

    String getDescription();
    
    Filter<FilterObjectType, ValueType> copy();
}
