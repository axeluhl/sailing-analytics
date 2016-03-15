package com.sap.sse.common.settings;

import java.util.UUID;

import com.sap.sse.common.settings.base.AbstractValueListSetting;
import com.sap.sse.common.settings.converter.UUIDConverter;

public class UUIDListSetting extends AbstractValueListSetting<UUID> {
    public UUIDListSetting(String name, AbstractSettings settings) {
        super(name, settings, UUIDConverter.INSTANCE);
    }
}
