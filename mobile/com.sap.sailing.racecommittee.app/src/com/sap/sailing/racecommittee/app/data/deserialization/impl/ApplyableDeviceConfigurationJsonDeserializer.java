package com.sap.sailing.racecommittee.app.data.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.racecommittee.app.domain.configuration.ApplyableDeviceConfiguration;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.ApplyableDeviceConfigurationImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceConfigurationJsonDeserializer;

public class ApplyableDeviceConfigurationJsonDeserializer extends DeviceConfigurationJsonDeserializer {
    
    @Override
    protected DeviceConfigurationImpl createDeviceConfiguration() {
        return new ApplyableDeviceConfigurationImpl();
    }

    public ApplyableDeviceConfiguration deserializeAsApplyable(JSONObject jsonObject) throws JsonDeserializationException {
        return (ApplyableDeviceConfiguration) deserialize(jsonObject);
    }

}
