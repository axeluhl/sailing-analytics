package com.sap.sailing.racecommittee.app.data.parsers;

import java.io.Reader;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.StoreableConfiguration;
import com.sap.sailing.racecommittee.app.data.deserialization.impl.StoredDeviceConfigurationJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;

public class DeviceConfigurationParser implements DataParser<StoreableConfiguration<DeviceConfiguration>> {
    
    private StoredDeviceConfigurationJsonDeserializer deserializer;
    
    public DeviceConfigurationParser(StoredDeviceConfigurationJsonDeserializer deserializer) {
        this.deserializer = deserializer;
    }

    @Override
    public StoreableConfiguration<DeviceConfiguration> parse(Reader reader) throws Exception {
        Object parsedResult = JSONValue.parseWithException(reader);
        JSONObject jsonObject = Helpers.toJSONObjectSafe(parsedResult);
        return deserializer.deserializeAsStored(jsonObject);
    }

}
