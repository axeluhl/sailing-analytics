package com.sap.sailing.gwt.autoplay.client.shared.header;

import com.sap.sse.common.settings.AbstractSettings;

public class SAPHeaderComponentSettings extends AbstractSettings {
    private final String title;

    /**
     *  The default settings
     */
    public SAPHeaderComponentSettings() {
        title = "";
    }
    
    public SAPHeaderComponentSettings(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
