package com.sap.sse.security.paywall.settings;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.converter.StringConverter;
import com.sap.sse.security.paywall.PaywallResolver;
import com.sap.sse.security.paywall.SecuredDTOProxy;
import com.sap.sse.security.shared.HasPermissions.Action;

public class SecuredStringSetting extends AbstractSecuredValueSetting<String> {

    public SecuredStringSetting(final String name, final AbstractGenericSerializableSettings settings,
            PaywallResolver paywallResolver, Action action, SecuredDTOProxy dtoContext) {
        this(name, settings, null, paywallResolver, action, dtoContext);
    }

    public SecuredStringSetting(final String name, final AbstractGenericSerializableSettings settings,
            final String defaultValue, PaywallResolver paywallResolver, Action action, SecuredDTOProxy dtoContext) {
        super(name, settings, defaultValue, StringConverter.INSTANCE, paywallResolver, action, dtoContext);
    }

    public boolean isNotBlank() {
        final String value = getValue();
        return value != null && !value.isEmpty();
    }
}
