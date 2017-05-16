package com.sap.sse.common.settings.generic;

import java.util.UUID;

import com.sap.sse.common.settings.generic.base.AbstractValueSetting;
import com.sap.sse.common.settings.generic.converter.UUIDConverter;

public class UUIDSetting extends AbstractValueSetting<UUID> {
    
    public UUIDSetting(String name, AbstractGenericSerializableSettings settings) {
        this(name, settings, null);
    }
    
    public UUIDSetting(String name, AbstractGenericSerializableSettings settings, UUID defaultValue) {
        super(name, settings, defaultValue, UUIDConverter.INSTANCE);
    }
}
