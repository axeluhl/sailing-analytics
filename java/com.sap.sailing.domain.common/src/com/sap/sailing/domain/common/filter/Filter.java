package com.sap.sailing.domain.common.filter;

import com.sap.sailing.domain.common.impl.Util.Pair;

/**
 * A filter base interface.
 */
public interface Filter<FilterObjectType, ValueType> {
    
    /**
     * @return All supported filter operators 
     */
    Iterable<FilterOperators> getSupportedOperators();

    /**
     * @return The default operator for the filter or null if there is no default operator.
     */
    FilterOperators getDefaultOperator();

    /**
     * @return The type of the value which is used to filter the object to filter  
     */
    Class<ValueType> getValueType();
    
    /**
     * @return The combination of filter operator to use and the filter value to use for the filtering
     */
    Pair<FilterOperators, ValueType> getConfiguration();

    /**
     * Sets the combination of filter operator to use and the filter value to use for the filtering
     */
    void setConfiguration(Pair<FilterOperators, ValueType> filterValueAndOperator);

    /**
     * 
     * @param the object to match against the filter
     * @return True if the object matches the filter criteria ('is filtered'), false otherwise
     */
    boolean matches(FilterObjectType object);
    
    /** 
     * @return The name of the filter
     */
    String getName();
    
    /**
     * @return a copy of the filter object
     */
    Filter<FilterObjectType, ValueType> copy();
}
