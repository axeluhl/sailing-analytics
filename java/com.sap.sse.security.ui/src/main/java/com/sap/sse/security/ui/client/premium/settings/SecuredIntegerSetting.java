package com.sap.sse.security.ui.client.premium.settings;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettingsWithContext;
import com.sap.sse.common.settings.generic.converter.IntegerConverter;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;

public class SecuredIntegerSetting extends AbstractSecuredValueSetting<Integer> {

    public SecuredIntegerSetting(String name, AbstractGenericSerializableSettingsWithContext<SecurityChildSettingsContext> settings,
            Action action) {
        this(name, settings, null, action);
    }

    public SecuredIntegerSetting(String name, AbstractGenericSerializableSettingsWithContext<SecurityChildSettingsContext> settings, Integer defaultValue,
            Action action) {
        super(name, settings, defaultValue, IntegerConverter.INSTANCE, action);
    }
}
