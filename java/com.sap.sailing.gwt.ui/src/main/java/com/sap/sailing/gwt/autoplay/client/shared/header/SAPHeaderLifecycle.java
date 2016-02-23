package com.sap.sailing.gwt.autoplay.client.shared.header;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class SAPHeaderLifecycle implements ComponentLifecycle<SAPHeader, SAPHeaderSettings, SAPHeaderSettingsDialogComponent> {
    private final StringMessages stringMessages;
    private final String defaultTitle;
    
    public SAPHeaderLifecycle(String defaultTitle, StringMessages stringMessages) {
        this.defaultTitle = defaultTitle;
        this.stringMessages = stringMessages;
    }
    
    @Override
    public SAPHeaderSettingsDialogComponent getSettingsDialogComponent(SAPHeaderSettings settings) {
        return new SAPHeaderSettingsDialogComponent(cloneSettings(settings), stringMessages);
    }

    @Override
    public SAPHeaderSettings createDefaultSettings() {
        return new SAPHeaderSettings(defaultTitle);
    }

    @Override
    public SAPHeaderSettings cloneSettings(SAPHeaderSettings settings) {
        return new SAPHeaderSettings(settings.getTitle());
    }

    @Override
    public String getLocalizedShortName() {
        return "SAP Header";
    }

    @Override
    public boolean hasSettings() {
        return true;
    }
}

