package com.sap.sailing.gwt.home.shared.places.user.profile.settings;

public class UserSettingsEntry {

    private final String key;
    private final String documentSettingsId;
    private final String profileData;
    private final String localData;

    public UserSettingsEntry(String key, String documentSettingsId, String profileData, String localData) {
        super();
        this.key = key;
        this.documentSettingsId = documentSettingsId;
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
}
