package com.sap.sse.gwt.client.shared.settings;

/**
 * Simple wrapper class which contains the storable representations of User Settings and Document Settings.
 * 
 * @author Vladislav Chumak
 * 
 *
 */
public class StorableRepresentationOfDocumentAndUserSettings {

    private final StorableSettingsRepresentation userSettingsRepresentation;
    private final StorableSettingsRepresentation documentSettingsRepresentation;

    /**
     * 
     * @param userSettingsRepresentation
     *            {@code null} indicates that there are no stored User Settings
     * @param documentSettingsRepresentation
     *            {@code null} indicates that there are no stored Document Settings
     */
    public StorableRepresentationOfDocumentAndUserSettings(StorableSettingsRepresentation userSettingsRepresentation,
            StorableSettingsRepresentation documentSettingsRepresentation) {
        this.userSettingsRepresentation = userSettingsRepresentation;
        this.documentSettingsRepresentation = documentSettingsRepresentation;
    }

    /**
     * 
     * @return {code null} if there are no stored User Settings, otherwise the stored representation of User Settings
     */
    public StorableSettingsRepresentation getUserSettingsRepresentation() {
        return userSettingsRepresentation;
    }

    /**
     * 
     * @return {code null} if there are no stored Document Settings, otherwise the stored representation of Document
     *         Settings
     */
    public StorableSettingsRepresentation getDocumentSettingsRepresentation() {
        return documentSettingsRepresentation;
    }
    
    /**
     * 
     * @return {@code true} if there are stored user settings, otherwise {@code false}
     */
    public boolean hasStoredUserSettings() {
        return userSettingsRepresentation != null;
    }
    
    /**
     * 
     * @return {@code true} if there are stored document settings, otherwise {@code false}
     */
    public boolean hasStoredDocumentSettings() {
        return documentSettingsRepresentation != null;
    }

}
