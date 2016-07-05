package com.sap.sse.security;

public interface PreferenceObjectListener<PrefT> {
    void preferenceObjectChanged(String username, String key, PrefT oldPreference, PrefT newPreference);
}
