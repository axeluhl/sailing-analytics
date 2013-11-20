package com.sap.sailing.racecommittee.app.data.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.StoreableConfiguration;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.PreferencesBasedDeviceConfiguration;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.PreferencesBasedRacingProceduresConfiguration;
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
        return new PreferencesBasedDeviceConfiguration(preferences, 
                (PreferencesBasedRacingProceduresConfiguration) proceduresConfiguration);
    }
    
    @SuppressWarnings("unchecked")
    public StoreableConfiguration<DeviceConfiguration> deserializeAsStored(JSONObject object) throws JsonDeserializationException {
        return (StoreableConfiguration<DeviceConfiguration>) super.deserialize(object);
    }

}
