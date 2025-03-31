package com.sap.sse.common.settings.generic;

import java.io.Serializable;
import java.util.Map;

import com.sap.sse.common.settings.Settings;

/**
 * {@link Settings} that need to be serialized and de-serialized to/from different formats. In a URL they should try to follow
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
 * {@link com.sap.sse.gwt.settings.SettingsToJsonSerializerGWT}: Serializes to GWT's JSON types. The resulting JSON
 * String is compatible with the backend-side SettingsToJsonSerializer.</li>
 * </ul>
 * 
 * In order to enable mapping the settings to URL parameters or JSON structures, each setting needs an associated name
 * and the {@link Settings} hierarchy must be ensured. For this every {@link Setting} has a constructor requiring it's
 * name and parent Settings object. To make {@link Settings} able to be nested in other {@link Settings} you need to
 * provide the very same constructor that can simply call super on {@link AbstractSettings}. This way, generic
 * serializers/de-serializers can take over the serialization from/to the various formats.
 */
public interface GenericSerializableSettings extends Settings, Setting, Serializable {
    
    /**
     * Path separator for serializers that construct flat keys for the hierarchical settings structures (e.g. URL).
     */
    public static final String PATH_SEPARATOR = ".";
    
    /**
     * When serializing {@link ValueCollectionSetting}s using diffing, this token is used as key for the added values.
     */
    public static final String ADDED_TOKEN = "added";
    
    /**
     * When serializing {@link ValueCollectionSetting}s using diffing, this token is used as key for the removed values.
     */
    public static final String REMOVED_TOKEN = "removed";
    
    /**
     * Obtains all child settings.
     */
    Map<String, Setting> getChildSettings();
}
