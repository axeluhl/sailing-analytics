package com.sap.sse.common.settings.generic;

import java.math.BigDecimal;

import com.sap.sse.common.settings.generic.base.AbstractValueListSetting;
import com.sap.sse.common.settings.generic.converter.DecimalConverter;

public class DecimalListSetting extends AbstractValueListSetting<BigDecimal> {
    public DecimalListSetting(String name, AbstractGenericSerializableSettings settings) {
        super(name, settings, DecimalConverter.INSTANCE);
    }
}
