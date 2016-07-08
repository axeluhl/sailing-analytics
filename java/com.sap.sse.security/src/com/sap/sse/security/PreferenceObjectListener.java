package com.sap.sse.security;

/**
 * Listener used by UserStore to notify about changes of stored preference objects.
 *
 * @param <PrefT> The type of preference object this Listener handles.
 */
public interface PreferenceObjectListener<PrefT> {
    void preferenceObjectChanged(String username, String key, PrefT oldPreference, PrefT newPreference);
}
