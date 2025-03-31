package com.sap.sse.common.settings.generic.base;

import java.util.LinkedHashSet;
import java.util.Set;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.generic.ValueSetSetting;
import com.sap.sse.common.settings.value.LinkedHashSetValue;
import com.sap.sse.common.settings.value.Value;
import com.sap.sse.common.settings.value.ValueCollectionValue;

public abstract class ValueLinkedHashSetSetting<T> extends AbstractValueCollectionSetting<T, Set<Value>, Set<T>> implements ValueSetSetting<T> {
    
    public ValueLinkedHashSetSetting(String name, AbstractGenericSerializableSettings settings, ValueConverter<T> valueConverter) {
        this(name, settings, false, valueConverter);
    }
    
    public ValueLinkedHashSetSetting(String name, AbstractGenericSerializableSettings settings, boolean emptyIsDefault, ValueConverter<T> valueConverter) {
        super(name, settings, valueConverter, emptyIsDefault);
    }
    
    public ValueLinkedHashSetSetting(String name, AbstractGenericSerializableSettings settings, Iterable<T> defaultValues, ValueConverter<T> valueConverter) {
        this(name, settings, defaultValues, false, valueConverter);
    }
    
    public ValueLinkedHashSetSetting(String name, AbstractGenericSerializableSettings settings, Iterable<T> defaultValues, boolean emptyIsDefault, ValueConverter<T> valueConverter) {
        this(name, settings, emptyIsDefault, valueConverter);
        setDefaultValues(defaultValues);
        resetToDefault();
    }
    
    @Override
    protected final ValueCollectionValue<Set<Value>> createValue() {
        return new LinkedHashSetValue();
    }
    
    @Override
    protected final Set<T> createDefaultValuesCollection() {
        return new LinkedHashSet<>();
    }
}
