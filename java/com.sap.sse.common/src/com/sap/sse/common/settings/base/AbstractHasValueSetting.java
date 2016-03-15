package com.sap.sse.common.settings.base;

import com.sap.sse.common.settings.AbstractSetting;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.HasValueSetting;
import com.sap.sse.common.settings.ValueConverter;

public abstract class AbstractHasValueSetting<T> extends AbstractSetting implements HasValueSetting<T> {
    
    private ValueConverter<T> valueConverter;
    
    public AbstractHasValueSetting(String name, AbstractSettings settings, ValueConverter<T> valueConverter) {
        super(name, settings);
        this.valueConverter = valueConverter;
    }
    
    @Override
    public ValueConverter<T> getValueConverter() {
        return valueConverter;
    }
}
