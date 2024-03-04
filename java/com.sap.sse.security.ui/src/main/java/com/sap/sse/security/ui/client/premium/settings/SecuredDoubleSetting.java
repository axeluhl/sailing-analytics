package com.sap.sse.security.ui.client.premium.settings;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.converter.DoubleConverter;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.premium.PaywallResolverProxy;
import com.sap.sse.security.ui.client.premium.SecuredDTOProxy;

public class SecuredDoubleSetting extends AbstractSecuredValueSetting<Double> {

    public SecuredDoubleSetting(String name, AbstractGenericSerializableSettings settings,
            PaywallResolverProxy paywallResolverProxy, Action action, SecuredDTOProxy dtoContext) {
        this(name, settings, null, paywallResolverProxy, action, dtoContext);
    }

    public SecuredDoubleSetting(String name, AbstractGenericSerializableSettings settings, Double defaultValue,
            PaywallResolverProxy paywallResolverProxy, Action action, SecuredDTOProxy dtoContext) {
        super(name, settings, defaultValue, DoubleConverter.INSTANCE, paywallResolverProxy, action, dtoContext);
    }
}
