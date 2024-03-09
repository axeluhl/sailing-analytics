package com.sap.sse.security.ui.client.premium.settings;

import java.util.UUID;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.converter.UUIDConverter;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;
import com.sap.sse.security.ui.client.premium.PaywallResolver;

public class SecuredUUIDSetting extends AbstractSecuredValueSetting<UUID> {

    public SecuredUUIDSetting(String name, AbstractGenericSerializableSettings<SecurityChildSettingsContext> settings,
            PaywallResolver paywallResolver, Action action, SecuredDTO securedDTO) {
        this(name, settings, null, paywallResolver, action, securedDTO);
    }

    public SecuredUUIDSetting(String name, AbstractGenericSerializableSettings<SecurityChildSettingsContext> settings, UUID defaultValue,
            PaywallResolver paywallResolver, Action action, SecuredDTO securedDTO) {
        super(name, settings, defaultValue, UUIDConverter.INSTANCE, paywallResolver, action, securedDTO);
    }
}
