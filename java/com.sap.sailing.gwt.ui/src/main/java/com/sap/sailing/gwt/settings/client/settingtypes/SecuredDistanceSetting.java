package com.sap.sailing.gwt.settings.client.settingtypes;

import com.sap.sailing.gwt.settings.client.settingtypes.converter.DistanceConverter;
import com.sap.sse.common.Distance;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettingsWithContext;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;
import com.sap.sse.security.ui.client.premium.settings.AbstractSecuredValueSetting;

public class SecuredDistanceSetting extends AbstractSecuredValueSetting<Distance> {
    public SecuredDistanceSetting(String name, AbstractGenericSerializableSettingsWithContext<SecurityChildSettingsContext> settings,
            Action action) {
        this(name, settings, null, action);
    }

    public SecuredDistanceSetting(String name, AbstractGenericSerializableSettingsWithContext<SecurityChildSettingsContext> settings, Distance defaultValue,
            Action action) {
        super(name, settings, defaultValue, DistanceConverter.INSTANCE, action);
    }

}
