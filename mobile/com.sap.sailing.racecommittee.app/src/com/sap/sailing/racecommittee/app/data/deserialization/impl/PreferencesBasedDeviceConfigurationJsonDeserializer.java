package com.sap.sailing.racecommittee.app.data.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.PreferencesBasedDeviceConfiguration;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.PreferencesBasedRacingProceduresConfiguration;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceConfigurationJsonDeserializer;

public class PreferencesBasedDeviceConfigurationJsonDeserializer extends DeviceConfigurationJsonDeserializer {

    public static PreferencesBasedDeviceConfigurationJsonDeserializer create(final AppPreferences preferences) {
        return new PreferencesBasedDeviceConfigurationJsonDeserializer(preferences,
                PreferencesBasedRacingProceduresConfigurationJsonDeserializer.create(preferences));
    }

    private final AppPreferences preferences;

    public PreferencesBasedDeviceConfigurationJsonDeserializer(AppPreferences preferences,
            PreferencesBasedRacingProceduresConfigurationJsonDeserializer proceduresDeserializer) {
        super(proceduresDeserializer);
        this.preferences = preferences;
    }

    @Override
    protected DeviceConfigurationImpl createConfiguration(RacingProceduresConfiguration proceduresConfiguration) {
        return new PreferencesBasedDeviceConfiguration(preferences,
                (PreferencesBasedRacingProceduresConfiguration) proceduresConfiguration);
    }

    public PreferencesBasedDeviceConfiguration deserializeAsStored(JSONObject object)
            throws JsonDeserializationException {
        return (PreferencesBasedDeviceConfiguration) deserialize(object);
    }

}
