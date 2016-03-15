package com.sap.sse.common.settings;

import com.sap.sse.common.settings.base.AbstractValueSetting;
import com.sap.sse.common.settings.converter.StringConverter;

public class StringSetting extends AbstractValueSetting<String> {
    
    public StringSetting(String name, AbstractSettings settings) {
        this(name, settings, null);
    }
    
    public StringSetting(String name, AbstractSettings settings, String defaultValue) {
        super(name, settings, defaultValue, StringConverter.INSTANCE);
    }
}
