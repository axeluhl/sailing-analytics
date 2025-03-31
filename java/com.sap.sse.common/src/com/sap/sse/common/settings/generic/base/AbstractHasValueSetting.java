package com.sap.sse.common.settings.generic.base;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.AbstractSetting;
import com.sap.sse.common.settings.generic.HasValueSetting;
import com.sap.sse.common.settings.generic.ValueConverter;

public abstract class AbstractHasValueSetting<T> extends AbstractSetting implements HasValueSetting<T> {
    
    private ValueConverter<T> valueConverter;
    
    public AbstractHasValueSetting(String name, AbstractGenericSerializableSettings settings, ValueConverter<T> valueConverter) {
        super(name, settings);
        this.valueConverter = valueConverter;
    }
    
    @Override
    public ValueConverter<T> getValueConverter() {
        return valueConverter;
    }
}
