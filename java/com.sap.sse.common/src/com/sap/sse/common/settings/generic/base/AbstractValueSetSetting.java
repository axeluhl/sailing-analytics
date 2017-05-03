package com.sap.sse.common.settings.generic.base;

import java.util.HashSet;
import java.util.Set;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.generic.ValueSetSetting;
import com.sap.sse.common.settings.value.Value;
import com.sap.sse.common.settings.value.ValueCollectionValue;
import com.sap.sse.common.settings.value.ValueSetValue;

public abstract class AbstractValueSetSetting<T> extends AbstractValueCollectionSetting<T, Set<Value>, Set<T>> implements ValueSetSetting<T> {
    
    public AbstractValueSetSetting(String name, AbstractGenericSerializableSettings settings, ValueConverter<T> valueConverter) {
        this(name, settings, false, valueConverter);
    }
    
    public AbstractValueSetSetting(String name, AbstractGenericSerializableSettings settings, boolean emptyIsDefault, ValueConverter<T> valueConverter) {
        super(name, settings, valueConverter, emptyIsDefault);
    }
    
    public AbstractValueSetSetting(String name, AbstractGenericSerializableSettings settings, Iterable<T> defaultValues, ValueConverter<T> valueConverter) {
        this(name, settings, defaultValues, false, valueConverter);
    }
    
    public AbstractValueSetSetting(String name, AbstractGenericSerializableSettings settings, Iterable<T> defaultValues, boolean emptyIsDefault, ValueConverter<T> valueConverter) {
        this(name, settings, emptyIsDefault, valueConverter);
        setDefaultValues(defaultValues);
        resetToDefault();
    }
    
    @Override
    protected final ValueCollectionValue<Set<Value>> createValue() {
        return new ValueSetValue();
    }
    
    @Override
    protected final Set<T> createDefaultValuesCollection() {
        return new HashSet<>();
    }
}
