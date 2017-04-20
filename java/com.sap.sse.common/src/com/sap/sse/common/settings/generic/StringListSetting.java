package com.sap.sse.common.settings.generic;

import com.sap.sse.common.settings.generic.base.AbstractValueListSetting;
import com.sap.sse.common.settings.generic.converter.StringConverter;

public class StringListSetting extends AbstractValueListSetting<String> {
    public StringListSetting(String name, AbstractGenericSerializableSettings settings) {
        super(name, settings, StringConverter.INSTANCE);
    }

    public StringListSetting(String name, AbstractGenericSerializableSettings settings, boolean emptyIsDefault) {
        super(name, settings, emptyIsDefault, StringConverter.INSTANCE);
    }

    public StringListSetting(String name, AbstractGenericSerializableSettings settings,
            Iterable<String> defaultValues) {
        super(name, settings, defaultValues, StringConverter.INSTANCE);
    }

    public StringListSetting(String name, AbstractGenericSerializableSettings settings, Iterable<String> defaultValues,
            boolean emptyIsDefault) {
        super(name, settings, defaultValues, emptyIsDefault, StringConverter.INSTANCE);
    }
}
