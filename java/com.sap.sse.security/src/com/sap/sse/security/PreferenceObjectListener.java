package com.sap.sse.security;

/**
 * Listener used by UserStore to notify about changes of stored preference objects.
 *
 * @param <PrefT>
 *            The type of preference object this Listener handles.
 */
public interface PreferenceObjectListener<PrefT> {
    /**
     * Called when the preference object for the given user and preference key has changed.
     * 
     * @param username
     *            the user for whom the preference object changed
     * @param key
     *            the key of the changed preference object
     * @param oldPreference
     *            the old state of the preference object. May be <code>null</code> if there was not preference object
     *            formerly.
     * @param newPreference
     *            the new state of the preference object. May be <code>null</code> if the preference object is being
     *            deleted.
     */
    void preferenceObjectChanged(String username, String key, PrefT oldPreference, PrefT newPreference);
}
