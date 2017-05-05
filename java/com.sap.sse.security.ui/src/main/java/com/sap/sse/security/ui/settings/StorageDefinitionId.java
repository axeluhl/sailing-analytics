package com.sap.sse.security.ui.settings;

import com.sap.sse.gwt.client.shared.settings.SettingsStorageManager;

/**
 * Represents the definition for User Settings and Document Settings storage keys, which may be required by
 * {@link SettingsStorageManager} implementations. The storage keys determine the settings which will be loaded and
 * stored in the context of the currently opened entry point, or rather root perspective/component.
 * 
 * @author Vladislav Chumak
 *
 */
public class StorageDefinitionId {

    private static final String PREFIX = "sailing.ui.usersettings.";

    private final String globalId;
    private final String contextSpecificId;

    /**
     * Constructs the storage definition according to the provided identifications for User Settings and Document
     * Settings in the context of the currently opened entry point.
     * 
     * @param globalId
     *            The storage key for User Settings, which is context independent
     * @param contextSpecificId
     *            The storage key for Document Settings, which is context dependent
     */
    public StorageDefinitionId(String globalId, String contextSpecificId) {
        this.globalId = globalId;
        this.contextSpecificId = contextSpecificId;
    }

    /**
     * Gets the identification of User Settings for the currently opened entry point, or rather root
     * perspective/component.
     * 
     * @return Storage id for User Settings
     */
    public String getGlobalId() {
        return globalId;
    }

    /**
     * Gets the identification of Document Settings for the currently opened entry point, or rather root
     * perspective/component, <b>AND</b> the context which is viewed by root perspective/component
     * 
     * @return Storage id for User Settings
     */
    public String getContextSpecificId() {
        return contextSpecificId;
    }

    public String generateStorageGlobalKey() {
        return PREFIX + globalId;
    }

    public String generateStorageContextSpecificKey() {
        return generateStorageGlobalKey() + "#" + contextSpecificId;
    }

    /**
     * Utility method used to build a context specific id for {@link StorageDefinitionId} construction.
     * 
     * @param contextDefinitionParameters
     *            The parameters which shape the context
     * @return The generated context definition id from the provided parameters
     */
    public static String buildContextDefinitionId(String... contextDefinitionParameters) {
        StringBuilder str = new StringBuilder("");
        boolean first = true;
        for (String contextDefinitionParameter : contextDefinitionParameters) {
            if (first) {
                first = false;
            } else {
                str.append(",");
            }
            if (contextDefinitionParameter != null) {
                str.append(contextDefinitionParameter);
            }
        }
        return str.toString();
    }

}
