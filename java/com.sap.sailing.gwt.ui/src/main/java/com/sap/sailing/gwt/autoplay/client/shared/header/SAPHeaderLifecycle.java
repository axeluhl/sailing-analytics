package com.sap.sailing.gwt.autoplay.client.shared.header;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class SAPHeaderLifecycle implements ComponentLifecycle<SAPHeader, AbstractSettings, SettingsDialogComponent<AbstractSettings>> {
    private SAPHeader component;
    
    public SAPHeaderLifecycle() {
        this.component = null;
    }
    
    @Override
    public SettingsDialogComponent<AbstractSettings> getSettingsDialogComponent(AbstractSettings settings) {
        return null;
    }

    @Override
    public SAPHeader getComponent() {
        return component;
    }

    @Override
    public AbstractSettings createDefaultSettings() {
        return null;
    }

    @Override
    public AbstractSettings cloneSettings(AbstractSettings settings) {
        return null;
    }

    @Override
    public String getLocalizedShortName() {
        return "Header";
    }

    @Override
    public boolean hasSettings() {
        return false;
    }
}

