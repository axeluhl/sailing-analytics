package com.sap.sse.common.settings.generic;

import com.sap.sse.common.settings.generic.base.AbstractValueListSetting;
import com.sap.sse.common.settings.generic.converter.DoubleConverter;

public class DoubleListSetting extends AbstractValueListSetting<Double> {
    public DoubleListSetting(String name, AbstractGenericSerializableSettings settings) {
        super(name, settings, DoubleConverter.INSTANCE);
    }
}
