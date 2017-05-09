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
public class StorageDefinition {

    private static final String PREFIX = "sailing.ui.usersettings.";

    private final String globalId;
    private final String contextSpecificId;

    /**
     * Constructs the storage definition according to the provided identifications for User Settings and Document
     * Settings in the context of the currently opened entry point.
     * 
     * @param globalId
     *            The storage id (without "sailing.ui.usersettings" prefix) for User Settings, which is context
     *            independent
     * @param contextSpecificId
     *            The storage id (without "sailing.ui.usersettings" prefix and {@code globalId} part) for Document
     *            Settings, which is context dependent
     */
    public StorageDefinition(String globalId, String contextSpecificId) {
        this.globalId = globalId;
        this.contextSpecificId = contextSpecificId;
    }

    /**
     * Generates the "primary key"-like identification of User Settings for the currently opened entry point, or rather
     * root perspective/component. {@code #PREFIX} and in the constructor provided {@link #globalId} are used.
     * 
     * @return Storage key for User Settings
     */
    public String generateStorageGlobalKey() {
        return PREFIX + globalId;
    }

    /**
     * Generates the "primary key"-like identification of Document Settings for the currently opened entry point, or
     * rather root perspective/component, <b>AND</b> the context which is viewed by root perspective/component.
     * {@code #PREFIX} and in the constructor provided {@link #globalId} and {@link #contextSpecificId} are used.
     * 
     * @return Storage key for Document Settings
     */
    public String generateStorageContextSpecificKey() {
        return generateStorageGlobalKey() + "#" + contextSpecificId;
    }

    /**
     * Utility method used to build a context specific id for {@link StorageDefinition} construction.
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
