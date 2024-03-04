package com.sap.sse.security.ui.client.premium.settings;

import java.util.UUID;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.converter.UUIDConverter;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.premium.PaywallResolverProxy;
import com.sap.sse.security.ui.client.premium.SecuredDTOProxy;

public class SecuredUUIDSetting extends AbstractSecuredValueSetting<UUID> {

    public SecuredUUIDSetting(String name, AbstractGenericSerializableSettings settings,
            PaywallResolverProxy paywallResolverProxy, Action action, SecuredDTOProxy dtoContext) {
        this(name, settings, null, paywallResolverProxy, action, dtoContext);
    }

    public SecuredUUIDSetting(String name, AbstractGenericSerializableSettings settings, UUID defaultValue,
            PaywallResolverProxy paywallResolverProxy, Action action, SecuredDTOProxy dtoContext) {
        super(name, settings, defaultValue, UUIDConverter.INSTANCE, paywallResolverProxy, action, dtoContext);
    }
}
