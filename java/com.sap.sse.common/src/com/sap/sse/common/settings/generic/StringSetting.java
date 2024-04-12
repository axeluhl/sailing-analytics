package com.sap.sse.common.settings.generic;

import com.sap.sse.common.settings.generic.base.AbstractValueSetting;
import com.sap.sse.common.settings.generic.converter.StringConverter;

public class StringSetting extends AbstractValueSetting<String> {

    public StringSetting(final String name, final AbstractGenericSerializableSettings settings) {
        this(name, settings, null);
    }

    public StringSetting(final String name, final AbstractGenericSerializableSettings settings, final String defaultValue) {
        super(name, settings, defaultValue, StringConverter.INSTANCE);
    }

    public boolean isNotBlank() {
        final String value = getValue();
        return value != null && !value.isEmpty();
    }
}
