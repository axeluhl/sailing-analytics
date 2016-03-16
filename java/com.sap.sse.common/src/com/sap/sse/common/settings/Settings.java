package com.sap.sse.common.settings;

import java.util.Map;

/**
 * A generic format to keep the settings for a <code>Component</code>. Such settings may be initialized with defaults.
 * Usually they can be edited by a user using a <code>SettingsDialogComponent</code> displayed by a
 * <code>SettingsDialog</code>. The interface is homed in this server-side, shared bundle because settings may as well
 * be handled on the server side, e.g., in order to store them in the user store or to apply them to a server-side
 * component.
 * <p>
 * 
 * Settings need to be serialized and de-serialized to/from different formats. In a URL they should try to follow
 * reasonable URL parameter encoding conventions and be human readable/editable. They may also be stored in a cookie or
 * in the user-specific preferences store where JSON may be a useful format to keep them as a {@link String}.
 * <p>
 * 
 * The following serializers are available:
 * <ul>
 * <li>
 * {@link com.sap.sse.common.settings.serializer.SettingsToStringMapSerializer}: Serializes to a Map containing String
 * keys and values</li>
 * <li>
 * {@link com.sap.sse.gwt.settings.SettingsToUrlSerializer}: Serializes to a GWT UrlBuilder</li>
 * <li>
 * {@link com.sap.sse.shared.settings.SettingsToJsonSerializer}: Serializes to org.json.simple objects usable in the
 * backend and on Android</li>
 * <li>
 * {@link com.sap.sse.gwt.settings.SettingsToJsonSerializerGWT}: Serializes to GWT's JSON types. The resliting JSON
 * String is compatible with the backend-side SettingsToJsonSerializer.</li>
 * </ul>
 * 
 * In order to enable mapping the settings to URL parameters or JSON structures, each setting needs an associated name
 * and the {@link Settings} hierarchy must be ensured. For this every {@link Setting} has a constructor requiring it's
 * name and parent Settings object. To make {@link Settings} able to be nested in other {@link Settings} you need to
 * provide the very same constructor that can simply call super on {@link AbstractSettings}. This way, generic
 * serializers/de-serializers can take over the serialization from/to the various formats.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Settings extends Setting {
    
    /**
     * Path separator for serializers that construct flat keys for the hierachical settings structures (e.g. URL).
     */
    public static final String PATH_SEPARATOR = ".";
    
    /**
     * Obtains all child settings.
     */
    Map<String, Setting> getChildSettings();
}
