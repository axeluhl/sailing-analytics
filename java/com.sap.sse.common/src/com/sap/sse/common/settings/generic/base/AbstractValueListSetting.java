package com.sap.sse.common.settings.generic.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sse.common.Util;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.generic.ValueListSetting;
import com.sap.sse.common.settings.value.Value;
import com.sap.sse.common.settings.value.ValueCollectionValue;
import com.sap.sse.common.settings.value.ValueListValue;

public abstract class AbstractValueListSetting<T> extends AbstractValueCollectionSetting<T, List<Value>>
        implements ValueListSetting<T> {

    private final List<T> defaultValues = new ArrayList<>();
    private final boolean emptyIsDefault;

    public AbstractValueListSetting(String name, AbstractGenericSerializableSettings settings,
            ValueConverter<T> valueConverter) {
        this(name, settings, false, valueConverter);
    }

    public AbstractValueListSetting(String name, AbstractGenericSerializableSettings settings, boolean emptyIsDefault,
            ValueConverter<T> valueConverter) {
        super(name, settings, valueConverter);
        this.emptyIsDefault = emptyIsDefault;
    }

    public AbstractValueListSetting(String name, AbstractGenericSerializableSettings settings,
            Iterable<T> defaultValues, ValueConverter<T> valueConverter) {
        this(name, settings, defaultValues, false, valueConverter);
    }

    public AbstractValueListSetting(String name, AbstractGenericSerializableSettings settings,
            Iterable<T> defaultValues, boolean emptyIsDefault, ValueConverter<T> valueConverter) {
        this(name, settings, emptyIsDefault, valueConverter);
        setDefaultValues(defaultValues);
        resetToDefault();
    }

    @Override
    protected ValueCollectionValue<List<Value>> createValue() {
        return new ValueListValue();
    }

    @Override
    public boolean isDefaultValue() {
        ValueCollectionValue<List<Value>> value = getValue();
        return (emptyIsDefault && (value == null || value.isEmpty()))
                || Util.equals(value.getValues(getValueConverter()), defaultValues);
    }

    @Override
    public Iterable<T> getValues() {
        ValueCollectionValue<List<Value>> value = getValue();
        if (emptyIsDefault && (value == null || value.isEmpty())) {
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
        if (defaultValues != null) {
            Util.addAll(defaultValues, this.defaultValues);
        }
        if (wasDefault) {
            resetToDefault();
        }
    }
}
