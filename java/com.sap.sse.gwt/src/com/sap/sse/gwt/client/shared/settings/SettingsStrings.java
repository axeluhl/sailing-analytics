package com.sap.sse.gwt.client.shared.settings;

/**
 * Simple class which wraps the JSON representations User Settings and Document Settings
 * as two {@link String}s.
 * 
 * @author Vladislav Chumak
 *
 */
public class SettingsStrings {

    private final String globalSettingsString;
    private final String contextSpecificSettingsString;
    
    public SettingsStrings(String globalSettingsString, String contextSpecificSettingsString) {
        this.globalSettingsString = globalSettingsString;
        this.contextSpecificSettingsString = contextSpecificSettingsString;
    }
    
    public String getGlobalSettingsString() {
        return globalSettingsString;
    }
    
    public String getContextSpecificSettingsString() {
        return contextSpecificSettingsString;
    }

}
