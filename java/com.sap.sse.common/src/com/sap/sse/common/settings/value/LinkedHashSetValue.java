package com.sap.sse.common.settings.value;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class LinkedHashSetValue extends ValueCollectionValue<Set<Value>> {
    private static final long serialVersionUID = -5820765644801217519L;
    
    public LinkedHashSetValue() {
        super(new LinkedHashSet<Value>());
    }
    
    @Override
    protected <T> Collection<T> emptyCollection() {
        return new LinkedHashSet<>();
    }
}
