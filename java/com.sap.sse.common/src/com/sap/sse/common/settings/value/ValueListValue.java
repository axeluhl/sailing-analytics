package com.sap.sse.common.settings.value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ValueListValue extends ValueCollectionValue<List<Value>> {
    private static final long serialVersionUID = -5820765644801217519L;
    
    public ValueListValue() {
        super(new ArrayList<Value>());
    }
    
    @Override
    protected <T> Collection<T> emptyCollection() {
        return new ArrayList<>();
    }
}
