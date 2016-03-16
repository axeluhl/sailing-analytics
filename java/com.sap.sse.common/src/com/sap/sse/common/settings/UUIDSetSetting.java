package com.sap.sse.common.settings;

import java.util.UUID;

import com.sap.sse.common.settings.base.AbstractValueSetSetting;
import com.sap.sse.common.settings.converter.UUIDConverter;

public class UUIDSetSetting extends AbstractValueSetSetting<UUID> {
    public UUIDSetSetting(String name, AbstractSettings settings) {
        super(name, settings, UUIDConverter.INSTANCE);
    }
    
    public UUIDSetSetting(String name, AbstractSettings settings, boolean emptyIsDefault) {
        super(name, settings, emptyIsDefault, UUIDConverter.INSTANCE);
    }

    public UUIDSetSetting(String name, AbstractSettings settings, Iterable<UUID> defaultValues) {
        super(name, settings, defaultValues, UUIDConverter.INSTANCE);
    }
    
    public UUIDSetSetting(String name, AbstractSettings settings, Iterable<UUID> defaultValues, boolean emptyIsDefault) {
        super(name, settings, defaultValues, emptyIsDefault, UUIDConverter.INSTANCE);
    }
}
