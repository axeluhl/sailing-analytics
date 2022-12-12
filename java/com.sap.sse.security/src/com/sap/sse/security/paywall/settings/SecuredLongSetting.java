package com.sap.sse.security.paywall.settings;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.converter.LongConverter;
import com.sap.sse.security.paywall.PaywallResolver;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;

public class SecuredLongSetting extends AbstractSecuredValueSetting<Long> {

    public SecuredLongSetting(String name, AbstractGenericSerializableSettings settings,
            PaywallResolver paywallResolver, Action action, SecuredDTO dtoContext) {
        this(name, settings, null, paywallResolver, action, dtoContext);
    }

    public SecuredLongSetting(String name, AbstractGenericSerializableSettings settings, Long defaultValue,
            PaywallResolver paywallResolver, Action action, SecuredDTO dtoContext) {
        super(name, settings, defaultValue, LongConverter.INSTANCE, paywallResolver, action, dtoContext);
    }
}
