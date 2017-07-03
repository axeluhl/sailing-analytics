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
public class StoredSettingsLocation {

    public static final String DOCUMENT_SETTINGS_SUFFIX_SEPARATOR = "#";

    private final String PREFIX;

    private final String userSettingsIdPart;
    private final String documentSettingsIdPart;

    /**
     * Constructs the storage definition according to the provided identifications for User Settings and Document
     * Settings in the context of the currently opened entry point.
     * 
     * @param userSettingsIdPart
     *            The storage id part (without "sailing.ui.usersettings" prefix) for User Settings, which is context
     *            independent
     * @param documentSettingsIdPart
     *            The storage id part (without "sailing.ui.usersettings" prefix and {@code userSettingsIdPart} part) for
     *            Document Settings, which is context dependent
     */
    public StoredSettingsLocation(String prefix, String userSettingsIdPart, String documentSettingsIdPart) {
        this.PREFIX = prefix;
        this.userSettingsIdPart = userSettingsIdPart;
        this.documentSettingsIdPart = documentSettingsIdPart;
    }

    /**
     * Generates the "primary key"-like identification of User Settings for the currently opened entry point, or rather
     * root perspective/component. {@code #PREFIX} and in the constructor provided {@link #userSettingsIdPart} are used.
     * 
     * @return Storage key for User Settings
     */
    public String generateStorageKeyForUserSettings() {
        return PREFIX + userSettingsIdPart;
    }

    /**
     * Generates the "primary key"-like identification of Document Settings for the currently opened entry point, or
     * rather root perspective/component, <b>AND</b> the context which is viewed by root perspective/component.
     * {@code #PREFIX} and in the constructor provided {@link #userSettingsIdPart} and {@link #documentSettingsIdPart}
     * are used.
     * 
     * @return Storage key for Document Settings
     */
    public String generateStorageKeyForDocumentSettings() {
        return generateStorageKeyForUserSettings() + DOCUMENT_SETTINGS_SUFFIX_SEPARATOR + documentSettingsIdPart;
    }

    /**
     * Utility method used to build a context specific id for {@link StoredSettingsLocation} construction.
     * 
     * @param contextDefinitionParameters
     *            The parameters which shape the context
     * @return The generated context definition id from the provided parameters
     */
    public static String buildDocumentSettingsIdPart(String... contextDefinitionParameters) {
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
