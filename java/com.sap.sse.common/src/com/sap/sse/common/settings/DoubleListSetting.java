package com.sap.sse.common.settings;

import com.sap.sse.common.settings.base.AbstractValueListSetting;
import com.sap.sse.common.settings.converter.DoubleConverter;

public class DoubleListSetting extends AbstractValueListSetting<Double> {
    public DoubleListSetting(String name, AbstractSettings settings) {
        super(name, settings, DoubleConverter.INSTANCE);
    }
}
