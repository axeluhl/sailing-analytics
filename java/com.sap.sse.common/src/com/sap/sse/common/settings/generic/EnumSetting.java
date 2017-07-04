package com.sap.sse.common.settings.generic;

import com.sap.sse.common.settings.generic.base.AbstractValueSetting;
import com.sap.sse.common.settings.generic.converter.EnumConverter;

public class EnumSetting<T extends Enum<T>> extends AbstractValueSetting<T> {
    
    public EnumSetting(String name, AbstractGenericSerializableSettings settings, StringToEnumConverter<T> stringToEnumConverter) {
        this(name, settings, null, stringToEnumConverter);
    }
    
    public EnumSetting(String name, AbstractGenericSerializableSettings settings, T defaultValue, StringToEnumConverter<T> stringToEnumConverter) {
        super(name, settings, defaultValue, new EnumConverter<>(stringToEnumConverter));
    }
}
