package com.sap.sailing.gwt.home.shared.places.user.profile.settings;

import com.sap.sse.security.ui.settings.StoredSettingsLocation;

public class UserSettingsEntry {

    private final String key;
    private final String keyWithoutContext;
    private final String documentSettingsId;
    private final String profileData;
    private final String localData;

    public UserSettingsEntry(String key, String profileData, String localData) {
        super();
        final int separatorIndex = key.indexOf(StoredSettingsLocation.DOCUMENT_SETTINGS_SUFFIX_SEPARATOR);
        if(separatorIndex > 0) {
            keyWithoutContext = key.substring(0, separatorIndex);
            documentSettingsId = key.substring(separatorIndex + 1);
        } else {
            keyWithoutContext = key;
            documentSettingsId = null;
        }
        this.key = key;
        this.profileData = profileData;
        this.localData = localData;
    }

    public String getKey() {
        return key;
    }

    public String getProfileData() {
        return profileData;
    }

    public String getLocalData() {
        return localData;
    }
    
    public String getDocumentSettingsId() {
        return documentSettingsId;
    }

    public String getKeyWithoutContext() {
        return keyWithoutContext;
    }
}
