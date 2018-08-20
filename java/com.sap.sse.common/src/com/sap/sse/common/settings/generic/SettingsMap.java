package com.sap.sse.common.settings.generic;

import java.util.Map;

import com.sap.sse.common.settings.Settings;

/**
 * This allows the generic serialization mechanism (see {@link GenericSerializableSettings}) to be used in dynamic
 * environments. An implementation of {@link SettingsMap} may contain {@link GenericSerializableSettings} but not the
 * other way around. The serializers will {@link SettingsMap}s and contained {@link SettingsMap} as well as
 * {@link GenericSerializableSettings} but anything else is being ignored. Be aware that such a combination of
 * {@link SettingsMap}s, {@link GenericSerializableSettings} and other {@link Settings} implementations can't be Java-
 * or GWT-RPC-serialized.
 */
public interface SettingsMap extends Settings {
    /**
     * @return the settings to be processes by serializer implementations.
     */
    Map<String, Settings> getSettingsPerComponentId();
}
