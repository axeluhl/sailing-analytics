package com.sap.sailing.gwt.settings.client.settingtypes;

import com.sap.sailing.gwt.settings.client.settingtypes.converter.DurationConverter;
import com.sap.sse.common.Duration;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;
import com.sap.sse.security.ui.client.premium.PaywallResolver;
import com.sap.sse.security.ui.client.premium.settings.AbstractSecuredValueSetting;

public class SecuredDurationSetting extends AbstractSecuredValueSetting<Duration> {
    public SecuredDurationSetting(String name, AbstractGenericSerializableSettings<SecurityChildSettingsContext> settings,
            PaywallResolver paywallResolver, Action action, SecuredDTO securedDto) {
        this(name, settings, null, paywallResolver, action, securedDto);
    }

    public SecuredDurationSetting(String name, AbstractGenericSerializableSettings<SecurityChildSettingsContext> settings, Duration defaultValue,
            PaywallResolver paywallResolver, Action action, SecuredDTO securedDto) {
        super(name, settings, defaultValue, DurationConverter.INSTANCE, paywallResolver, action, securedDto);
    }

}
