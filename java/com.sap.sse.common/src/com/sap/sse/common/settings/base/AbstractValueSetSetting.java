package com.sap.sse.common.settings.base;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.common.Util;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.ValueConverter;
import com.sap.sse.common.settings.ValueSetSetting;

public abstract class AbstractValueSetSetting<T> extends AbstractValueCollectionSetting<T> implements ValueSetSetting<T> {
    
    private final Set<T> values = new HashSet<>();
    private final Set<T> defaultValues = new HashSet<>();
    private final boolean emptyIsDefault;
    
    public AbstractValueSetSetting(String name, AbstractSettings settings, ValueConverter<T> valueConverter) {
        this(name, settings, false, valueConverter);
    }
    
    public AbstractValueSetSetting(String name, AbstractSettings settings, boolean emptyIsDefault, ValueConverter<T> valueConverter) {
        super(name, settings, valueConverter);
        this.emptyIsDefault = emptyIsDefault;
    }
    
    public AbstractValueSetSetting(String name, AbstractSettings settings, Iterable<T> defaultValues, ValueConverter<T> valueConverter) {
        this(name, settings, defaultValues, false, valueConverter);
    }
    
    public AbstractValueSetSetting(String name, AbstractSettings settings, Iterable<T> defaultValues, boolean emptyIsDefault, ValueConverter<T> valueConverter) {
        this(name, settings, emptyIsDefault, valueConverter);
        setDefaultValues(defaultValues);
        resetToDefault();
    }
    
    @Override
    protected Collection<T> getInnerCollection() {
        return values;
    }
    
    @Override
    public boolean isDefaultValue() {
        return (emptyIsDefault && values.isEmpty())
                || (values.size() == defaultValues.size() && values.containsAll(defaultValues));
    }
    
    @Override
    public Iterable<T> getValues() {
        if(emptyIsDefault && values.isEmpty()) {
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
