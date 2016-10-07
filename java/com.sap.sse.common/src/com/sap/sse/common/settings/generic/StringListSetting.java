package com.sap.sse.common.settings.generic;

import com.sap.sse.common.settings.generic.base.AbstractValueListSetting;
import com.sap.sse.common.settings.generic.converter.StringConverter;

public class StringListSetting extends AbstractValueListSetting<String> {
    public StringListSetting(String name, AbstractGenericSerializableSettings settings) {
        super(name, settings, StringConverter.INSTANCE);
    }
}
