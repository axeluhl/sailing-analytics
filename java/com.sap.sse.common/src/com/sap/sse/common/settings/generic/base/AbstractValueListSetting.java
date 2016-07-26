package com.sap.sse.common.settings.generic.base;

import java.util.List;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.value.Value;
import com.sap.sse.common.settings.value.ValueCollectionValue;
import com.sap.sse.common.settings.value.ValueListValue;

public abstract class AbstractValueListSetting<T> extends AbstractValueCollectionSetting<T, List<Value>> {
    public AbstractValueListSetting(String name, AbstractGenericSerializableSettings settings, ValueConverter<T> valueConverter) {
        super(name, settings, valueConverter);
    }
    
    @Override
    protected ValueCollectionValue<List<Value>> createValue() {
        return new ValueListValue();
    }
    
    @Override
    public void resetToDefault() {
        clear();
    }
    
    @Override
    public boolean isDefaultValue() {
        ValueCollectionValue<List<Value>> value = getValue();
        // explicit default values are possible to implement
        // currently, empty is always default
        return value == null || value.isEmpty();
    }
}
