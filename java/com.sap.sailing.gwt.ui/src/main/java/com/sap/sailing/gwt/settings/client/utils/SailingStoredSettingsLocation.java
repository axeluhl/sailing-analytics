package com.sap.sailing.gwt.settings.client.utils;

import com.sap.sailing.gwt.ui.shared.settings.SailingSettingsConstants;
import com.sap.sse.security.ui.settings.StoredSettingsLocation;

public class SailingStoredSettingsLocation extends StoredSettingsLocation {
    
    /**
     * @see com.sap.sse.security.ui.settings.StoredSettingsLocation#StoredSettingsLocation(String, String)
     */
    public SailingStoredSettingsLocation(String userSettingsIdPart, String documentSettingsIdPart) {
        super(SailingSettingsConstants.USER_SETTINGS_UI, userSettingsIdPart, documentSettingsIdPart);
    }

}
