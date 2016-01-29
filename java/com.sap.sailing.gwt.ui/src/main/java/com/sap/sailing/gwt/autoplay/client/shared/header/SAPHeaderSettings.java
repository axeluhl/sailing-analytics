package com.sap.sailing.gwt.autoplay.client.shared.header;

import com.sap.sse.common.settings.AbstractSettings;

public class SAPHeaderSettings extends AbstractSettings {
    private final String title;

    /**
     *  The default settings
     */
    public SAPHeaderSettings() {
        title = "";
    }
    
    public SAPHeaderSettings(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
