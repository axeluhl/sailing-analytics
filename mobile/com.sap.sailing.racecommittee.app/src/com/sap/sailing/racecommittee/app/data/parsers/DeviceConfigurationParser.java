package com.sap.sailing.racecommittee.app.data.parsers;

import java.io.Reader;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sap.sailing.racecommittee.app.data.deserialization.impl.ApplyableDeviceConfigurationJsonDeserializer;
import com.sap.sailing.racecommittee.app.domain.configuration.ApplyableDeviceConfiguration;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;

public class DeviceConfigurationParser implements DataParser<ApplyableDeviceConfiguration> {
    
    private ApplyableDeviceConfigurationJsonDeserializer deserializer;
    
    public DeviceConfigurationParser(ApplyableDeviceConfigurationJsonDeserializer deserializer) {
        this.deserializer = deserializer;
    }

    @Override
    public ApplyableDeviceConfiguration parse(Reader reader) throws Exception {
        Object parsedResult = JSONValue.parseWithException(reader);
        JSONObject jsonObject = Helpers.toJSONObjectSafe(parsedResult);
        return deserializer.deserializeAsApplyable(jsonObject);
    }

}
