package com.sap.sailing.domain.common.filter;

import java.util.Collection;


public interface CollectionFilter<FilterObjectType, ValueType> extends Filter<FilterObjectType, ValueType> {
    Collection<FilterObjectType> filter(Collection<FilterObjectType> objectCollection);
}
 