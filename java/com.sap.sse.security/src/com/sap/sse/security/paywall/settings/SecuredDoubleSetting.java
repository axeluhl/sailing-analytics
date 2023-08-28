package com.sap.sse.security.paywall.settings;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.converter.DoubleConverter;
import com.sap.sse.security.paywall.PaywallResolver;
import com.sap.sse.security.paywall.SecuredDTOProxy;
import com.sap.sse.security.shared.HasPermissions.Action;

public class SecuredDoubleSetting extends AbstractSecuredValueSetting<Double> {

    public SecuredDoubleSetting(String name, AbstractGenericSerializableSettings settings,
            PaywallResolver paywallResolver, Action action, SecuredDTOProxy dtoContext) {
        this(name, settings, null, paywallResolver, action, dtoContext);
    }

    public SecuredDoubleSetting(String name, AbstractGenericSerializableSettings settings, Double defaultValue,
            PaywallResolver paywallResolver, Action action, SecuredDTOProxy dtoContext) {
        super(name, settings, defaultValue, DoubleConverter.INSTANCE, paywallResolver, action, dtoContext);
    }
}
