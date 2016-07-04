package com.sap.sse.security;

public interface PreferenceConverter<PREF> {
    String toString(PREF preference);

    PREF toPreference(String stringPreference);
}
