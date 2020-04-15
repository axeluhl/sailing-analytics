package com.sap.sailing.server.impl;

import java.util.function.Supplier;

import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.security.interfaces.PreferenceConverter;
import com.sap.sse.shared.settings.SettingsToJsonSerializer;

public class GenericJSONPreferenceConverter<PREF extends GenericSerializableSettings> implements PreferenceConverter<PREF> {
    
    private final SettingsToJsonSerializer serializer = new SettingsToJsonSerializer();
    private final Supplier<PREF> emptyInstanceFactory;
    
    public GenericJSONPreferenceConverter(Supplier<PREF> emptyInstanceFactory) {
        this.emptyInstanceFactory = emptyInstanceFactory;
    }

    @Override
    public String toPreferenceString(PREF preference) {
        return serializer.serializeToString(preference);
    }

    @Override
    public PREF toPreferenceObject(String stringPreference) {
        return serializer.deserialize(emptyInstanceFactory.get(), stringPreference);
    }

}
