package com.sap.sse.common.settings.value;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ValueSetValue extends ValueCollectionValue<Set<Value>> {
    private static final long serialVersionUID = -5820765644801217519L;
    
    public ValueSetValue() {
        super(new HashSet<Value>());
    }
    
    @Override
    protected <T> Collection<T> emptyCollection() {
        return new HashSet<>();
    }
}
