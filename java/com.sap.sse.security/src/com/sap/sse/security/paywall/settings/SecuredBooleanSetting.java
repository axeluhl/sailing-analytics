package com.sap.sse.security.paywall.settings;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.converter.BooleanConverter;
import com.sap.sse.security.paywall.PaywallResolver;
import com.sap.sse.security.paywall.SecuredDTOProxy;
import com.sap.sse.security.shared.HasPermissions.Action;

public class SecuredBooleanSetting extends AbstractSecuredValueSetting<Boolean> {

    public SecuredBooleanSetting(String name, AbstractGenericSerializableSettings settings, PaywallResolver paywallResolver,
            Action action, SecuredDTOProxy dtoContext) {
        this(name, settings, null, paywallResolver, action, dtoContext);
    }

    public SecuredBooleanSetting(String name, AbstractGenericSerializableSettings settings, Boolean defaultValue,
            PaywallResolver paywallResolver, Action action, SecuredDTOProxy securedDTO) {
        super(name, settings, defaultValue, BooleanConverter.INSTANCE, paywallResolver, action, securedDTO);
    }
}
