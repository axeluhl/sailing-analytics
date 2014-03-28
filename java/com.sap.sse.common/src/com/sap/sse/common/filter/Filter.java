package com.sap.sse.common.filter;


/**
 * A filter base interface.
 */
public interface Filter<FilterObjectType> {
    /**
     * 
     * @param the object to match against the filter
     * @return True if the object matches the filter criteria ('is not filtered'), false otherwise
     */
    boolean matches(FilterObjectType object);
    
    /** 
     * @return The name of the filter
     */
    String getName();
}
