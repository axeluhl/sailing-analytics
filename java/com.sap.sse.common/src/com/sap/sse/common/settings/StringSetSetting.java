package com.sap.sse.common.settings;

import com.sap.sse.common.settings.base.AbstractValueSetSetting;
import com.sap.sse.common.settings.converter.StringConverter;

public class StringSetSetting extends AbstractValueSetSetting<String> {
    public StringSetSetting(String name, AbstractSettings settings) {
        super(name, settings, StringConverter.INSTANCE);
    }

    public StringSetSetting(String name, AbstractSettings settings, Iterable<String> defaultValues) {
        super(name, settings, defaultValues, StringConverter.INSTANCE);
    }
}
