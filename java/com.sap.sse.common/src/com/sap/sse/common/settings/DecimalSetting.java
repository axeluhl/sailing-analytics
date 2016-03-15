package com.sap.sse.common.settings;

import java.math.BigDecimal;

import com.sap.sse.common.settings.base.AbstractValueSetting;
import com.sap.sse.common.settings.converter.DecimalConverter;

public class DecimalSetting extends AbstractValueSetting<BigDecimal> {
    
    public DecimalSetting(String name, AbstractSettings settings) {
        this(name, settings, null);
    }
    
    public DecimalSetting(String name, AbstractSettings settings, BigDecimal defaultValue) {
        super(name, settings, defaultValue, DecimalConverter.INSTANCE);
    }
}
