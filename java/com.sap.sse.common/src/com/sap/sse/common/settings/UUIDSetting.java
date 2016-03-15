package com.sap.sse.common.settings;

import java.util.UUID;

import com.sap.sse.common.settings.base.AbstractValueSetting;
import com.sap.sse.common.settings.converter.UUIDConverter;

public class UUIDSetting extends AbstractValueSetting<UUID> {
    
    public UUIDSetting(String name, AbstractSettings settings) {
        this(name, settings, null);
    }
    
    public UUIDSetting(String name, AbstractSettings settings, UUID defaultValue) {
        super(name, settings, defaultValue, UUIDConverter.INSTANCE);
    }
}
