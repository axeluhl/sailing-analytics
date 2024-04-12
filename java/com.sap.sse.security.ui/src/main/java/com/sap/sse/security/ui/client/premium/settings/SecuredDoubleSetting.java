package com.sap.sse.security.ui.client.premium.settings;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettingsWithContext;
import com.sap.sse.common.settings.generic.converter.DoubleConverter;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;
import com.sap.sse.security.ui.client.premium.PaywallResolver;

public class SecuredDoubleSetting extends AbstractSecuredValueSetting<Double> {

    public SecuredDoubleSetting(String name, AbstractGenericSerializableSettingsWithContext<SecurityChildSettingsContext> settings,
            PaywallResolver paywallResolver, Action action, SecuredDTO securedDTO) {
        this(name, settings, null, paywallResolver, action, securedDTO);
    }

    public SecuredDoubleSetting(String name, AbstractGenericSerializableSettingsWithContext<SecurityChildSettingsContext> settings, Double defaultValue,
            PaywallResolver paywallResolver, Action action, SecuredDTO securedDTO) {
        super(name, settings, defaultValue, DoubleConverter.INSTANCE, paywallResolver, action, securedDTO);
    }
}
