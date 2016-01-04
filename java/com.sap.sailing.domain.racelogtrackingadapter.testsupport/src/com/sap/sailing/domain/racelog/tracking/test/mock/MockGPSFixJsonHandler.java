package com.sap.sailing.domain.racelog.tracking.test.mock;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixMovingJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixMovingJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.GPSFixJsonHandler;

public class MockGPSFixJsonHandler implements GPSFixJsonHandler {

    @Override
    public JSONObject transformForth(GPSFix object) {
        return new GPSFixMovingJsonSerializer().serialize((GPSFixMoving) object);
    }

    @Override
    public GPSFix transformBack(JSONObject json) throws JsonDeserializationException {
        return new GPSFixMovingJsonDeserializer().deserialize(json);
    }

}
