package com.sap.sse.common.settings.generic.base;

import java.util.ArrayList;
import java.util.List;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.generic.ValueListSetting;
import com.sap.sse.common.settings.value.Value;
import com.sap.sse.common.settings.value.ValueCollectionValue;
import com.sap.sse.common.settings.value.ValueListValue;

public abstract class AbstractValueListSetting<T> extends AbstractValueCollectionSetting<T, List<Value>, List<T>>
        implements ValueListSetting<T> {

    public AbstractValueListSetting(String name, AbstractGenericSerializableSettings settings,
            ValueConverter<T> valueConverter) {
        this(name, settings, false, valueConverter);
    }

    public AbstractValueListSetting(String name, AbstractGenericSerializableSettings settings, boolean emptyIsDefault,
            ValueConverter<T> valueConverter) {
        super(name, settings, valueConverter, emptyIsDefault);
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
    protected final ValueCollectionValue<List<Value>> createValue() {
        return new ValueListValue();
    }
    
    @Override
    protected final List<T> createDefaultValuesCollection() {
        return new ArrayList<>();
    }
}
