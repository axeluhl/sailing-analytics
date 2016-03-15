package com.sap.sse.common.settings;

import com.sap.sse.common.settings.base.AbstractValueListSetting;
import com.sap.sse.common.settings.converter.StringConverter;

public class StringListSetting extends AbstractValueListSetting<String> {
    public StringListSetting(String name, AbstractSettings settings) {
        super(name, settings, StringConverter.INSTANCE);
    }
}
