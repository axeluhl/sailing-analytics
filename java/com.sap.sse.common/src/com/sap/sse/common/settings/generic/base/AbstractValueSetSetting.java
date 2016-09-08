package com.sap.sse.common.settings.generic.base;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.common.Util;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.generic.ValueSetSetting;
import com.sap.sse.common.settings.value.Value;
import com.sap.sse.common.settings.value.ValueCollectionValue;
import com.sap.sse.common.settings.value.ValueSetValue;

public abstract class AbstractValueSetSetting<T> extends AbstractValueCollectionSetting<T, Set<Value>> implements ValueSetSetting<T> {
    
    private final Set<T> defaultValues = new HashSet<>();
    private final boolean emptyIsDefault;
    
    public AbstractValueSetSetting(String name, AbstractGenericSerializableSettings settings, ValueConverter<T> valueConverter) {
        this(name, settings, false, valueConverter);
    }
    
    public AbstractValueSetSetting(String name, AbstractGenericSerializableSettings settings, boolean emptyIsDefault, ValueConverter<T> valueConverter) {
        super(name, settings, valueConverter);
        this.emptyIsDefault = emptyIsDefault;
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
    protected ValueCollectionValue<Set<Value>> createValue() {
        return new ValueSetValue();
    }
    
    @Override
    public boolean isDefaultValue() {
        ValueCollectionValue<Set<Value>> value = getValue();
        return (emptyIsDefault && (value == null || value.isEmpty()))
                || (value.size() == defaultValues.size() && defaultValues.containsAll(value.getValues(getValueConverter())));
    }
    
    @Override
    public Iterable<T> getValues() {
        ValueCollectionValue<Set<Value>> value = getValue();
        if(emptyIsDefault && (value == null || value.isEmpty())) {
            return Collections.unmodifiableCollection(defaultValues);
        }
        return super.getValues();
    }
    
    @Override
    public void resetToDefault() {
        setValues(defaultValues);
    }
    
    @Override
    public final void setDefaultValues(Iterable<T> defaultValues) {
        boolean wasDefault = isDefaultValue();
        this.defaultValues.clear();
        if(defaultValues != null) {
            Util.addAll(defaultValues, this.defaultValues);
        }
        if(wasDefault) {
            resetToDefault();
        }
    }
}
