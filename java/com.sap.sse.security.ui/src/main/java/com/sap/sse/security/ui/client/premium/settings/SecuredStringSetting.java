package com.sap.sse.security.ui.client.premium.settings;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettingsWithContext;
import com.sap.sse.common.settings.generic.converter.StringConverter;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;
import com.sap.sse.security.ui.client.premium.PaywallResolver;

public class SecuredStringSetting extends AbstractSecuredValueSetting<String> {

    public SecuredStringSetting(final String name, final AbstractGenericSerializableSettingsWithContext<SecurityChildSettingsContext> settings,
            PaywallResolver paywallResolver, Action action, SecuredDTO securedDTO) {
        this(name, settings, null, paywallResolver, action, securedDTO);
    }

    public SecuredStringSetting(final String name, final AbstractGenericSerializableSettingsWithContext<SecurityChildSettingsContext> settings,
            final String defaultValue, PaywallResolver paywallResolver, Action action, SecuredDTO securedDTO) {
        super(name, settings, defaultValue, StringConverter.INSTANCE, paywallResolver, action, securedDTO);
    }

    public boolean isNotBlank() {
        final String value = getValue();
        return value != null && !value.isEmpty();
    }
}
