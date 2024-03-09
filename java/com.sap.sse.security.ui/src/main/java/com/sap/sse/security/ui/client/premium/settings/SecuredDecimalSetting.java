package com.sap.sse.security.ui.client.premium.settings;

import java.math.BigDecimal;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.converter.DecimalConverter;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;
import com.sap.sse.security.ui.client.premium.PaywallResolver;

public class SecuredDecimalSetting extends AbstractSecuredValueSetting<BigDecimal> {
    
    public SecuredDecimalSetting(String name, AbstractGenericSerializableSettings<SecurityChildSettingsContext> settings, PaywallResolver paywallResolver,
            Action action, SecuredDTO securedDTO) {
        this(name, settings, null, paywallResolver, action, securedDTO);
    }

    public SecuredDecimalSetting(String name, AbstractGenericSerializableSettings<SecurityChildSettingsContext> settings, BigDecimal defaultValue,
            PaywallResolver paywallResolver, Action action, SecuredDTO securedDTO) {
        super(name, settings, defaultValue, DecimalConverter.INSTANCE, paywallResolver, action, securedDTO);
    }
}
