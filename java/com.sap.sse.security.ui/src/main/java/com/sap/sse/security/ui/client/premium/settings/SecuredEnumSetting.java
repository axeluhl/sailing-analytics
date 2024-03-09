package com.sap.sse.security.ui.client.premium.settings;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.StringToEnumConverter;
import com.sap.sse.common.settings.generic.converter.EnumConverter;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;
import com.sap.sse.security.ui.client.premium.PaywallResolver;

public class SecuredEnumSetting<T extends Enum<T>> extends AbstractSecuredValueSetting<T> {

    public SecuredEnumSetting(String name, AbstractGenericSerializableSettings<SecurityChildSettingsContext> settings,
            StringToEnumConverter<T> stringToEnumConverter, PaywallResolver paywallResolver, Action action,
            SecuredDTO securedDTO) {
        this(name, settings, null, stringToEnumConverter, paywallResolver, action, securedDTO);
    }

    public SecuredEnumSetting(String name, AbstractGenericSerializableSettings<SecurityChildSettingsContext> settings, T defaultValue,
            StringToEnumConverter<T> stringToEnumConverter, PaywallResolver paywallResolver, Action action,
            SecuredDTO securedDTO) {
        super(name, settings, defaultValue, new EnumConverter<>(stringToEnumConverter), paywallResolver, action,
                securedDTO);
    }
}
