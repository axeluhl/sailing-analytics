package com.sap.sse.security.paywall.settings;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.StringToEnumConverter;
import com.sap.sse.common.settings.generic.converter.EnumConverter;
import com.sap.sse.security.paywall.PaywallResolver;
import com.sap.sse.security.paywall.SecuredDTOProxy;
import com.sap.sse.security.shared.HasPermissions.Action;

public class SecuredEnumSetting<T extends Enum<T>> extends AbstractSecuredValueSetting<T> {

    public SecuredEnumSetting(String name, AbstractGenericSerializableSettings settings,
            StringToEnumConverter<T> stringToEnumConverter, PaywallResolver paywallResolver, Action action,
            SecuredDTOProxy dtoContext) {
        this(name, settings, null, stringToEnumConverter, paywallResolver, action, dtoContext);
    }

    public SecuredEnumSetting(String name, AbstractGenericSerializableSettings settings, T defaultValue,
            StringToEnumConverter<T> stringToEnumConverter, PaywallResolver paywallResolver, Action action,
            SecuredDTOProxy dtoContext) {
        super(name, settings, defaultValue, new EnumConverter<>(stringToEnumConverter), paywallResolver, action,
                dtoContext);
    }
}
