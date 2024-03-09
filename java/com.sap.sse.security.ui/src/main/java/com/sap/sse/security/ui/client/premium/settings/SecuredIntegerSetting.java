package com.sap.sse.security.ui.client.premium.settings;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.converter.IntegerConverter;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;
import com.sap.sse.security.ui.client.premium.PaywallResolver;

public class SecuredIntegerSetting extends AbstractSecuredValueSetting<Integer> {

    public SecuredIntegerSetting(String name, AbstractGenericSerializableSettings<SecurityChildSettingsContext> settings,
            PaywallResolver paywallResolver, Action action, SecuredDTO securedDTO) {
        this(name, settings, null, paywallResolver, action, securedDTO);
    }

    public SecuredIntegerSetting(String name, AbstractGenericSerializableSettings<SecurityChildSettingsContext> settings, Integer defaultValue,
            PaywallResolver paywallResolver, Action action, SecuredDTO securedDTO) {
        super(name, settings, defaultValue, IntegerConverter.INSTANCE, paywallResolver, action, securedDTO);
    }
}
