package com.sap.sse.security.ui.client.premium.settings;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettingsWithContext;
import com.sap.sse.common.settings.generic.converter.BooleanConverter;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;
import com.sap.sse.security.ui.client.premium.PaywallResolver;

public class SecuredBooleanSetting extends AbstractSecuredValueSetting<Boolean> {

    public SecuredBooleanSetting(String name, AbstractGenericSerializableSettingsWithContext<SecurityChildSettingsContext> settings,
            PaywallResolver paywallResolver, Action action, SecuredDTO securedDto) {
        this(name, settings, null, paywallResolver, action, securedDto);
    }

    public SecuredBooleanSetting(String name, AbstractGenericSerializableSettingsWithContext<SecurityChildSettingsContext> settings, Boolean defaultValue,
            PaywallResolver paywallResolver, Action action, SecuredDTO securedDto) {
        super(name, settings, defaultValue, BooleanConverter.INSTANCE, paywallResolver, action, securedDto);
    }
}
