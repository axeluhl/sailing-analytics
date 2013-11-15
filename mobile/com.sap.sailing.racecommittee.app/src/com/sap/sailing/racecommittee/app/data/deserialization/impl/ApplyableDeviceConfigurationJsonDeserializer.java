package com.sap.sailing.racecommittee.app.data.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.domain.configuration.ApplyableDeviceConfiguration;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.ApplyableDeviceConfigurationImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceConfigurationJsonDeserializer;

public class ApplyableDeviceConfigurationJsonDeserializer extends DeviceConfigurationJsonDeserializer {
    
    private final AppPreferences preferences;
    
    public ApplyableDeviceConfigurationJsonDeserializer(AppPreferences preferences) {
        this.preferences = preferences;
    }
    
    @Override
    protected DeviceConfigurationImpl createDeviceConfiguration() {
        return new ApplyableDeviceConfigurationImpl(preferences);
    }

    public ApplyableDeviceConfiguration deserializeAsApplyable(JSONObject jsonObject) throws JsonDeserializationException {
        return (ApplyableDeviceConfiguration) deserialize(jsonObject);
    }

}
