package com.sap.sse.security.paywall.settings;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.converter.DoubleConverter;
import com.sap.sse.security.paywall.PaywallResolver;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;

public class SecuredDoubleSetting extends AbstractSecuredValueSetting<Double> {

    public SecuredDoubleSetting(String name, AbstractGenericSerializableSettings settings,
            PaywallResolver paywallResolver, Action action, SecuredDTO dtoContext) {
        this(name, settings, null, paywallResolver, action, dtoContext);
    }

    public SecuredDoubleSetting(String name, AbstractGenericSerializableSettings settings, Double defaultValue,
            PaywallResolver paywallResolver, Action action, SecuredDTO dtoContext) {
        super(name, settings, defaultValue, DoubleConverter.INSTANCE, paywallResolver, action, dtoContext);
    }
}
