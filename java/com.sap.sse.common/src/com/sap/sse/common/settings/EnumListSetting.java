package com.sap.sse.common.settings;

import com.sap.sse.common.settings.base.AbstractValueListSetting;
import com.sap.sse.common.settings.converter.EnumConverter;

public class EnumListSetting<T extends Enum<T>> extends AbstractValueListSetting<T> {
    public EnumListSetting(String name, AbstractSettings settings, StringToEnumConverter<T> stringToEnumConverter) {
        super(name, settings, new EnumConverter<>(stringToEnumConverter));
    }
}
