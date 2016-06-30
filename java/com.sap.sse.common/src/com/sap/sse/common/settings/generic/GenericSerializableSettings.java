package com.sap.sse.common.settings.generic;

import java.io.Serializable;
import java.util.Map;

import com.sap.sse.common.settings.Settings;

public interface GenericSerializableSettings extends Settings, Setting, Serializable {
    
    /**
     * Path separator for serializers that construct flat keys for the hierachical settings structures (e.g. URL).
     */
    public static final String PATH_SEPARATOR = ".";
    
    /**
     * Obtains all child settings.
     */
    Map<String, Setting> getChildSettings();
}
