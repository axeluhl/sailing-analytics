package com.sap.sse.common.settings.generic;

import com.sap.sse.common.settings.generic.base.AbstractValueSetting;
import com.sap.sse.common.settings.generic.converter.IntegerConverter;

public class IntegerSetting extends AbstractValueSetting<Integer> {
    
    public IntegerSetting(String name, AbstractGenericSerializableSettings settings) {
        this(name, settings, null);
    }
    
    public IntegerSetting(String name, AbstractGenericSerializableSettings settings, Integer defaultValue) {
        super(name, settings, defaultValue, IntegerConverter.INSTANCE);
    }
}
