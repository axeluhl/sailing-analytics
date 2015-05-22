package com.sap.sailing.racecommittee.app.data.parsers;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceConfigurationJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.Reader;

public class DeviceConfigurationParser implements DataParser<DeviceConfiguration> {
    
    private DeviceConfigurationJsonDeserializer deserializer;
    
    public DeviceConfigurationParser(DeviceConfigurationJsonDeserializer deserializer) {
        this.deserializer = deserializer;
    }

    @Override
    public DeviceConfiguration parse(Reader reader) throws Exception {
        Object parsedResult = JSONValue.parseWithException(reader);
        JSONObject jsonObject = Helpers.toJSONObjectSafe(parsedResult);
        return deserializer.deserialize(jsonObject);
    }

}
