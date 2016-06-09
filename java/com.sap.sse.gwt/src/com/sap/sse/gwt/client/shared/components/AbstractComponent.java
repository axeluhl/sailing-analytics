package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.Settings;

public abstract class AbstractComponent<SettingsType extends Settings> implements Component<SettingsType> {
    @Override
    public String getId() {
        return getLocalizedShortName();
    }
}
