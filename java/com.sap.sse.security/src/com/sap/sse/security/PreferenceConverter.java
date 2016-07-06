package com.sap.sse.security;

/**
 * Converter to be used by {@link UserStore} to convert preference objects to/from Strings.
 * @see UserStore#registerPreferenceConverter(String, PreferenceConverter)
 *
 * @param <PREF> The type of preference object, this converter handles.
 */
public interface PreferenceConverter<PREF> {
    String toString(PREF preference);

    PREF toPreference(String stringPreference);
}
