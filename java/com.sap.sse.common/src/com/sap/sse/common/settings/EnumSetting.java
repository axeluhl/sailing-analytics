package com.sap.sse.common.settings;

import com.sap.sse.common.settings.base.AbstractValueSetting;
import com.sap.sse.common.settings.converter.EnumConverter;

public class EnumSetting<T extends Enum<T>> extends AbstractValueSetting<T> {
    
    public EnumSetting(String name, AbstractSettings settings, StringToEnumConverter<T> stringToEnumConverter) {
        this(name, settings, null, stringToEnumConverter);
    }
    
    public EnumSetting(String name, AbstractSettings settings, T defaultValue, StringToEnumConverter<T> stringToEnumConverter) {
        super(name, settings, defaultValue, new EnumConverter<>(stringToEnumConverter));
    }
}
