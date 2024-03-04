package com.sap.sse.security.ui.client.premium.settings;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.converter.StringConverter;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.premium.PaywallResolverProxy;
import com.sap.sse.security.ui.client.premium.SecuredDTOProxy;

public class SecuredStringSetting extends AbstractSecuredValueSetting<String> {

    public SecuredStringSetting(final String name, final AbstractGenericSerializableSettings settings,
            PaywallResolverProxy paywallResolverProxy, Action action, SecuredDTOProxy dtoContext) {
        this(name, settings, null, paywallResolverProxy, action, dtoContext);
    }

    public SecuredStringSetting(final String name, final AbstractGenericSerializableSettings settings,
            final String defaultValue, PaywallResolverProxy paywallResolverProxy, Action action, SecuredDTOProxy dtoContext) {
        super(name, settings, defaultValue, StringConverter.INSTANCE, paywallResolverProxy, action, dtoContext);
    }

    public boolean isNotBlank() {
        final String value = getValue();
        return value != null && !value.isEmpty();
    }
}
