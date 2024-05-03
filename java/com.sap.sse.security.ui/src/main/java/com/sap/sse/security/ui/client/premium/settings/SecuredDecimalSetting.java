package com.sap.sse.security.ui.client.premium.settings;

import java.math.BigDecimal;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettingsWithContext;
import com.sap.sse.common.settings.generic.converter.DecimalConverter;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;

public class SecuredDecimalSetting extends AbstractSecuredValueSetting<BigDecimal> {
    
    public SecuredDecimalSetting(String name, AbstractGenericSerializableSettingsWithContext<SecurityChildSettingsContext> settings,
            Action action) {
        this(name, settings, null, action);
    }

    public SecuredDecimalSetting(String name, AbstractGenericSerializableSettingsWithContext<SecurityChildSettingsContext> settings, BigDecimal defaultValue,
            Action action) {
        super(name, settings, defaultValue, DecimalConverter.INSTANCE, action);
    }
}
