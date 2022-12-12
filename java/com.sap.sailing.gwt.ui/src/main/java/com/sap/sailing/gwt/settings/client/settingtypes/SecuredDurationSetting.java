package com.sap.sailing.gwt.settings.client.settingtypes;

import com.sap.sailing.gwt.settings.client.settingtypes.converter.DurationConverter;
import com.sap.sse.common.Duration;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.security.paywall.PaywallResolver;
import com.sap.sse.security.paywall.settings.AbstractSecuredValueSetting;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;

public class SecuredDurationSetting extends AbstractSecuredValueSetting<Duration> {
    public SecuredDurationSetting(String name, AbstractGenericSerializableSettings settings,
            PaywallResolver paywallResolver, Action action, SecuredDTO dtoContext) {
        this(name, settings, null, paywallResolver, action, dtoContext);
    }

    public SecuredDurationSetting(String name, AbstractGenericSerializableSettings settings, Duration defaultValue,
            PaywallResolver paywallResolver, Action action, SecuredDTO dtoContext) {
        super(name, settings, defaultValue, DurationConverter.INSTANCE, paywallResolver, action, dtoContext);
    }

}
