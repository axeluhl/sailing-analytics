package com.sap.sse.common.settings.generic;

import com.sap.sse.common.settings.generic.base.AbstractValueListSetting;
import com.sap.sse.common.settings.generic.converter.EnumConverter;

public class EnumListSetting<T extends Enum<T>> extends AbstractValueListSetting<T> {
    public EnumListSetting(String name, AbstractGenericSerializableSettings settings,
            StringToEnumConverter<T> stringToEnumConverter) {
        super(name, settings, new EnumConverter<>(stringToEnumConverter));
    }

    public EnumListSetting(String name, AbstractGenericSerializableSettings settings, boolean emptyIsDefault,
            StringToEnumConverter<T> stringToEnumConverter) {
        super(name, settings, emptyIsDefault, new EnumConverter<>(stringToEnumConverter));
    }

    public EnumListSetting(String name, AbstractGenericSerializableSettings settings, Iterable<T> defaultValues,
            StringToEnumConverter<T> stringToEnumConverter) {
        super(name, settings, defaultValues, new EnumConverter<>(stringToEnumConverter));
    }

    public EnumListSetting(String name, AbstractGenericSerializableSettings settings, Iterable<T> defaultValues,
            boolean emptyIsDefault, StringToEnumConverter<T> stringToEnumConverter) {
        super(name, settings, defaultValues, emptyIsDefault, new EnumConverter<>(stringToEnumConverter));
    }
}
