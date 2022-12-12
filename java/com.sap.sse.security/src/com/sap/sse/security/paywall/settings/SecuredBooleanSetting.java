package com.sap.sse.security.paywall.settings;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.converter.BooleanConverter;
import com.sap.sse.security.paywall.PaywallResolver;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;

public class SecuredBooleanSetting extends AbstractSecuredValueSetting<Boolean> {

    public SecuredBooleanSetting(String name, AbstractGenericSerializableSettings settings, PaywallResolver dtoContext,
            Action action, SecuredDTO paywallResolver) {
        this(name, settings, null, paywallResolver, action, dtoContext);
    }

    public SecuredBooleanSetting(String name, AbstractGenericSerializableSettings settings, Boolean defaultValue,
            SecuredDTO dtoContext, Action action, PaywallResolver paywallResolver) {
        super(name, settings, defaultValue, BooleanConverter.INSTANCE, paywallResolver, action, dtoContext);
    }
}
