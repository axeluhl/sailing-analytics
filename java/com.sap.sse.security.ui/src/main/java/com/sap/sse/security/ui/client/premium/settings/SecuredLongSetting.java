package com.sap.sse.security.ui.client.premium.settings;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.converter.LongConverter;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.premium.PaywallResolverProxy;
import com.sap.sse.security.ui.client.premium.SecuredDTOProxy;

public class SecuredLongSetting extends AbstractSecuredValueSetting<Long> {

    public SecuredLongSetting(String name, AbstractGenericSerializableSettings settings,
            PaywallResolverProxy paywallResolverProxy, Action action, SecuredDTOProxy dtoContext) {
        this(name, settings, null, paywallResolverProxy, action, dtoContext);
    }

    public SecuredLongSetting(String name, AbstractGenericSerializableSettings settings, Long defaultValue,
            PaywallResolverProxy paywallResolverProxy, Action action, SecuredDTOProxy dtoContext) {
        super(name, settings, defaultValue, LongConverter.INSTANCE, paywallResolverProxy, action, dtoContext);
    }
}
