package com.sap.sse.common.settings.generic;

import com.sap.sse.common.settings.generic.base.AbstractValueSetting;
import com.sap.sse.common.settings.generic.converter.DoubleConverter;

public class DoubleSetting extends AbstractValueSetting<Double> {
    
    public DoubleSetting(String name, AbstractGenericSerializableSettings settings) {
        this(name, settings, null);
    }
    
    public DoubleSetting(String name, AbstractGenericSerializableSettings settings, Double defaultValue) {
        super(name, settings, defaultValue, DoubleConverter.INSTANCE);
    }
}
