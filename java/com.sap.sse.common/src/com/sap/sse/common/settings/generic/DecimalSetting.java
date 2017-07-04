package com.sap.sse.common.settings.generic;

import java.math.BigDecimal;

import com.sap.sse.common.settings.generic.base.AbstractValueSetting;
import com.sap.sse.common.settings.generic.converter.DecimalConverter;

public class DecimalSetting extends AbstractValueSetting<BigDecimal> {
    
    public DecimalSetting(String name, AbstractGenericSerializableSettings settings) {
        this(name, settings, null);
    }
    
    public DecimalSetting(String name, AbstractGenericSerializableSettings settings, BigDecimal defaultValue) {
        super(name, settings, defaultValue, DecimalConverter.INSTANCE);
    }
}
