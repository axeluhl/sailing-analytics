package com.sap.sse.security.ui.client.premium.settings;

import java.math.BigDecimal;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.converter.DecimalConverter;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.premium.PaywallResolverProxy;
import com.sap.sse.security.ui.client.premium.SecuredDTOProxy;

public class SecuredDecimalSetting extends AbstractSecuredValueSetting<BigDecimal> {
    
    public SecuredDecimalSetting(String name, AbstractGenericSerializableSettings settings, PaywallResolverProxy paywallResolverProxy,
            Action action, SecuredDTOProxy dtoContext) {
        this(name, settings, null, paywallResolverProxy, action, dtoContext);
    }

    public SecuredDecimalSetting(String name, AbstractGenericSerializableSettings settings, BigDecimal defaultValue,
            PaywallResolverProxy paywallResolverProxy, Action action, SecuredDTOProxy dtoContext) {
        super(name, settings, defaultValue, DecimalConverter.INSTANCE, paywallResolverProxy, action, dtoContext);
    }
}
