package com.sap.sailing.gwt.settings.client.settingtypes;

import com.sap.sailing.gwt.settings.client.settingtypes.converter.DistanceConverter;
import com.sap.sse.common.Distance;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.security.paywall.PaywallResolver;
import com.sap.sse.security.paywall.settings.AbstractSecuredValueSetting;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;

public class SecuredDistanceSetting extends AbstractSecuredValueSetting<Distance> {
    public SecuredDistanceSetting(String name, AbstractGenericSerializableSettings settings,
            PaywallResolver paywallResolver, Action action, SecuredDTO dtoContext) {
        this(name, settings, null, paywallResolver, action, dtoContext);
    }

    public SecuredDistanceSetting(String name, AbstractGenericSerializableSettings settings, Distance defaultValue,
            PaywallResolver paywallResolver, Action action, SecuredDTO dtoContext) {
        super(name, settings, defaultValue, DistanceConverter.INSTANCE, paywallResolver, action, dtoContext);
    }

}
