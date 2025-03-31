package com.sap.sse.common.settings.generic;

import com.sap.sse.common.settings.Settings;

/**
 * 
 * Used by {@link SettingsList} for deserialization of complex {@link Settings} based collections.
 * Because GWT can't use reflection the serialization mechanism must rely on factories to create nested instances on demand.
 * 
 * @param <T> The type of the created {@link Settings} objects
 */
public interface SettingsFactory<T extends Settings> {
    T newInstance();
}
