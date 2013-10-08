package com.sap.sailing.racecommittee.app.data.parsers;

import java.io.Reader;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sap.sailing.domain.base.TabletConfiguration;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;

public class TabletConfigurationParser implements DataParser<TabletConfiguration> {
    
    private JsonDeserializer<TabletConfiguration> deserializer;
    
    public TabletConfigurationParser(JsonDeserializer<TabletConfiguration> deserializer) {
        this.deserializer = deserializer;
    }

    @Override
    public TabletConfiguration parse(Reader reader) throws Exception {
        Object parsedResult = JSONValue.parseWithException(reader);
        JSONObject jsonObject = Helpers.toJSONObjectSafe(parsedResult);
        return deserializer.deserialize(jsonObject);
    }

}
