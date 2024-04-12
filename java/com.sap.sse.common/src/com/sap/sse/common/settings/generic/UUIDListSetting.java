package com.sap.sse.common.settings.generic;

import java.util.UUID;

import com.sap.sse.common.settings.generic.base.AbstractValueListSetting;
import com.sap.sse.common.settings.generic.converter.UUIDConverter;

public class UUIDListSetting extends AbstractValueListSetting<UUID> {
    public UUIDListSetting(String name, AbstractGenericSerializableSettings settings) {
        super(name, settings, UUIDConverter.INSTANCE);
    }
}
