package com.sap.sse.common.settings.generic;

import com.sap.sse.common.settings.generic.base.AbstractValueSetting;
import com.sap.sse.common.settings.generic.converter.LongConverter;

public class LongSetting extends AbstractValueSetting<Long> {
    
    public LongSetting(String name, AbstractGenericSerializableSettings settings) {
        this(name, settings, null);
    }
    
    public LongSetting(String name, AbstractGenericSerializableSettings settings, Long defaultValue) {
        super(name, settings, defaultValue, LongConverter.INSTANCE);
    }
}
