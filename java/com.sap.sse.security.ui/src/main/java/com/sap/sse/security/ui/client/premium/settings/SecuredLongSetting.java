package com.sap.sse.security.ui.client.premium.settings;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettingsWithContext;
import com.sap.sse.common.settings.generic.converter.LongConverter;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;
import com.sap.sse.security.ui.client.premium.PaywallResolver;

public class SecuredLongSetting extends AbstractSecuredValueSetting<Long> {

    public SecuredLongSetting(String name, AbstractGenericSerializableSettingsWithContext<SecurityChildSettingsContext> settings,
            PaywallResolver paywallResolver, Action action, SecuredDTO securedDTO) {
        this(name, settings, null, paywallResolver, action, securedDTO);
    }

    public SecuredLongSetting(String name, AbstractGenericSerializableSettingsWithContext<SecurityChildSettingsContext> settings, Long defaultValue,
            PaywallResolver paywallResolver, Action action, SecuredDTO securedDTO) {
        super(name, settings, defaultValue, LongConverter.INSTANCE, paywallResolver, action, securedDTO);
    }
}
