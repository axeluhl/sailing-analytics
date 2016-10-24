package com.sap.sailing.racecommittee.app.data.parsers;

import java.io.Reader;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sap.sailing.domain.base.impl.RaceColumnFactorImpl;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.deserialization.impl.RaceColumnFactorJsonDeserializer;

public class RaceColumnsParser implements DataParser<RaceColumnFactorImpl> {

    private RaceColumnFactorJsonDeserializer deserializer;

    public RaceColumnsParser(RaceColumnFactorJsonDeserializer deserializer) {
        this.deserializer = deserializer;
    }

    @Override
    public RaceColumnFactorImpl parse(Reader reader) throws Exception {
        Object parsedResult = JSONValue.parseWithException(reader);
        JSONObject jsonObject = Helpers.toJSONObjectSafe(parsedResult);
        return deserializer.deserialize(jsonObject);
    }
}
