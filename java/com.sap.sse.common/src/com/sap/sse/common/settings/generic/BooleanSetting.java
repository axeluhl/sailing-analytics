package com.sap.sse.common.settings.generic;

import com.sap.sse.common.settings.generic.base.AbstractValueSetting;
import com.sap.sse.common.settings.generic.converter.BooleanConverter;

public class BooleanSetting extends AbstractValueSetting<Boolean> {

    public BooleanSetting(String name, AbstractGenericSerializableSettings settings) {
        this(name, settings, null);
    }

    public BooleanSetting(String name, AbstractGenericSerializableSettings settings, Boolean defaultValue) {
        super(name, settings, defaultValue, BooleanConverter.INSTANCE);
    }
}
