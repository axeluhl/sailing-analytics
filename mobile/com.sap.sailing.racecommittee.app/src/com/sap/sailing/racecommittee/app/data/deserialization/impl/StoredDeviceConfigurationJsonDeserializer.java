package com.sap.sailing.racecommittee.app.data.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.StoredDeviceConfiguration;
import com.sap.sailing.domain.base.configuration.StoredRacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.StoredDeviceConfigurationImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceConfigurationJsonDeserializer;

public class StoredDeviceConfigurationJsonDeserializer extends DeviceConfigurationJsonDeserializer {

    public static StoredDeviceConfigurationJsonDeserializer create(final AppPreferences preferences) {
        return new StoredDeviceConfigurationJsonDeserializer(preferences, 
                new StoredRacingProceduresConfigurationJsonDeserializer(preferences));
    }
    
    private final AppPreferences preferences;

    public StoredDeviceConfigurationJsonDeserializer(AppPreferences preferences,
            StoredRacingProceduresConfigurationJsonDeserializer proceduresDeserializer) {
        super(proceduresDeserializer);
        this.preferences = preferences;
    }

    @Override
    protected DeviceConfigurationImpl createConfiguration(RacingProceduresConfiguration proceduresConfiguration) {
        return new StoredDeviceConfigurationImpl(preferences,
                (StoredRacingProceduresConfiguration) proceduresConfiguration);
    }
    
    @Override
    public StoredDeviceConfiguration deserialize(JSONObject object) throws JsonDeserializationException {
        return (StoredDeviceConfiguration) super.deserialize(object);
    }

}
