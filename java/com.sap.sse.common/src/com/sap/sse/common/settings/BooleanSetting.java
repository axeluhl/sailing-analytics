package com.sap.sse.common.settings;

import com.sap.sse.common.settings.base.AbstractValueSetting;
import com.sap.sse.common.settings.converter.BooleanConverter;

public class BooleanSetting extends AbstractValueSetting<Boolean> {

    public BooleanSetting(String name, AbstractSettings settings) {
        this(name, settings, null);
    }

    public BooleanSetting(String name, AbstractSettings settings, Boolean defaultValue) {
        super(name, settings, defaultValue, BooleanConverter.INSTANCE);
    }
}
