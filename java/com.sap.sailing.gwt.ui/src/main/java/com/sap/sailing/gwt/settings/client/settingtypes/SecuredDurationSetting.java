package com.sap.sailing.gwt.settings.client.settingtypes;

import com.sap.sailing.gwt.settings.client.settingtypes.converter.DurationConverter;
import com.sap.sse.common.Duration;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettingsWithContext;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;
import com.sap.sse.security.ui.client.premium.settings.AbstractSecuredValueSetting;

public class SecuredDurationSetting extends AbstractSecuredValueSetting<Duration> {
    public SecuredDurationSetting(String name, AbstractGenericSerializableSettingsWithContext<SecurityChildSettingsContext> settings,
            Action action) {
        this(name, settings, null, action);
    }

    public SecuredDurationSetting(String name, AbstractGenericSerializableSettingsWithContext<SecurityChildSettingsContext> settings, Duration defaultValue,
            Action action) {
        super(name, settings, defaultValue, DurationConverter.INSTANCE, action);
    }

}
