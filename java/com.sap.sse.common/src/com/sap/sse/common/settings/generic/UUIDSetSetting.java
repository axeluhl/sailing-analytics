package com.sap.sse.common.settings.generic;

import java.util.UUID;

import com.sap.sse.common.settings.generic.base.AbstractValueSetSetting;
import com.sap.sse.common.settings.generic.converter.UUIDConverter;

public class UUIDSetSetting extends AbstractValueSetSetting<UUID> {
    public UUIDSetSetting(String name, AbstractGenericSerializableSettings settings) {
        super(name, settings, UUIDConverter.INSTANCE);
    }
    
    public UUIDSetSetting(String name, AbstractGenericSerializableSettings settings, boolean emptyIsDefault) {
        super(name, settings, emptyIsDefault, UUIDConverter.INSTANCE);
    }

    public UUIDSetSetting(String name, AbstractGenericSerializableSettings settings, Iterable<UUID> defaultValues) {
        super(name, settings, defaultValues, UUIDConverter.INSTANCE);
    }
    
    public UUIDSetSetting(String name, AbstractGenericSerializableSettings settings, Iterable<UUID> defaultValues, boolean emptyIsDefault) {
        super(name, settings, defaultValues, emptyIsDefault, UUIDConverter.INSTANCE);
    }
}
