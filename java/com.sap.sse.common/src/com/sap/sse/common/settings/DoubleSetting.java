package com.sap.sse.common.settings;

import com.sap.sse.common.settings.base.AbstractValueSetting;
import com.sap.sse.common.settings.converter.DoubleConverter;

public class DoubleSetting extends AbstractValueSetting<Double> {
    
    public DoubleSetting(String name, AbstractSettings settings) {
        this(name, settings, null);
    }
    
    public DoubleSetting(String name, AbstractSettings settings, Double defaultValue) {
        super(name, settings, defaultValue, DoubleConverter.INSTANCE);
    }
}
