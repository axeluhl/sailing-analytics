package com.sap.sse.security.paywall.settings;

import java.util.UUID;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.converter.UUIDConverter;
import com.sap.sse.security.paywall.PaywallResolver;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;

public class SecuredUUIDSetting extends AbstractSecuredValueSetting<UUID> {

    public SecuredUUIDSetting(String name, AbstractGenericSerializableSettings settings,
            PaywallResolver paywallResolver, Action action, SecuredDTO dtoContext) {
        this(name, settings, null, paywallResolver, action, dtoContext);
    }

    public SecuredUUIDSetting(String name, AbstractGenericSerializableSettings settings, UUID defaultValue,
            PaywallResolver paywallResolver, Action action, SecuredDTO dtoContext) {
        super(name, settings, defaultValue, UUIDConverter.INSTANCE, paywallResolver, action, dtoContext);
    }
}
