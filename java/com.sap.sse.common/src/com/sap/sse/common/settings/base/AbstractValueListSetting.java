package com.sap.sse.common.settings.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.ValueConverter;

public abstract class AbstractValueListSetting<T> extends AbstractValueCollectionSetting<T> {
    
    private final List<T> values = new ArrayList<>();
    
    public AbstractValueListSetting(String name, AbstractSettings settings, ValueConverter<T> valueConverter) {
        super(name, settings, valueConverter);
    }
    
    @Override
    protected Collection<T> getInnerCollection() {
        return values;
    }
    
    @Override
    public void resetToDefault() {
        values.clear();
    }
    
    @Override
    public boolean isDefaultValue() {
        // explicit default values are possible to implement
        // currently, empty is always default
        return getInnerCollection().isEmpty();
    }
}
