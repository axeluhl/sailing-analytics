package com.sap.sse.common.settings;

import java.math.BigDecimal;

import com.sap.sse.common.settings.base.AbstractValueListSetting;
import com.sap.sse.common.settings.converter.DecimalConverter;

public class DecimalListSetting extends AbstractValueListSetting<BigDecimal> {
    public DecimalListSetting(String name, AbstractSettings settings) {
        super(name, settings, DecimalConverter.INSTANCE);
    }
}
