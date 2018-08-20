package com.sap.sse.common.settings.generic;

import com.sap.sse.common.settings.generic.base.AbstractValueSetting;
import com.sap.sse.common.settings.generic.converter.StringConverter;

public class StringSetting extends AbstractValueSetting<String> {
    
    public StringSetting(String name, AbstractGenericSerializableSettings settings) {
        this(name, settings, null);
    }
    
    public StringSetting(String name, AbstractGenericSerializableSettings settings, String defaultValue) {
        super(name, settings, defaultValue, StringConverter.INSTANCE);
    }
}
