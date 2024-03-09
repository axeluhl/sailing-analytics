package com.sap.sailing.gwt.settings.client.settingtypes;

import com.sap.sailing.gwt.settings.client.settingtypes.converter.DistanceConverter;
import com.sap.sse.common.Distance;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;
import com.sap.sse.security.ui.client.premium.PaywallResolver;
import com.sap.sse.security.ui.client.premium.settings.AbstractSecuredValueSetting;

public class SecuredDistanceSetting extends AbstractSecuredValueSetting<Distance> {
    public SecuredDistanceSetting(String name, AbstractGenericSerializableSettings<SecurityChildSettingsContext> settings,
            PaywallResolver paywallResolver, Action action, SecuredDTO securedDTO) {
        this(name, settings, null, paywallResolver, action, securedDTO);
    }

    public SecuredDistanceSetting(String name, AbstractGenericSerializableSettings<SecurityChildSettingsContext> settings, Distance defaultValue,
            PaywallResolver paywallResolver, Action action, SecuredDTO securedDTO) {
        super(name, settings, defaultValue, DistanceConverter.INSTANCE, paywallResolver, action, securedDTO);
    }

}
