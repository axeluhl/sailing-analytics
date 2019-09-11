package com.sap.sse.security.interfaces;

/**
 * Converter to be used by {@link UserStore} to convert preference objects to/from Strings.
 * 
 * @see UserStore#registerPreferenceConverter(String, PreferenceConverter)
 *
 * @param <PREF>
 *            The type of preference object, this converter handles.
 */
public interface PreferenceConverter<PREF> {
    /**
     * To be used when publishing {@link PreferenceConverter} instances to the OSGi service registry. A property with
     * this key needs to be added to the publication that defines the preference key for which the specific converter
     * instance should be registered at the {@link UserStore}.
     */
    public final String KEY_PARAMETER_NAME = "key";
    
    String toPreferenceString(PREF preference);

    PREF toPreferenceObject(String stringPreference);
}
