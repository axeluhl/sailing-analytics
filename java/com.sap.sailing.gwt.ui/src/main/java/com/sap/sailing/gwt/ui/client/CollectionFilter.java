package com.sap.sailing.gwt.ui.client;

import java.util.Collection;

public interface CollectionFilter<FilterObjectType, ValueType> extends Filter<FilterObjectType, ValueType> {
    Collection<FilterObjectType> filter(Collection<FilterObjectType> objectCollection);
}
 