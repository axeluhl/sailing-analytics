package com.sap.sailing.server.gateway.deserialization.coursedata.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Gate;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.BaseControlPointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.GateJsonSerializer;

/**
 * Deserializer for gates.
 */
public class GateDeserializer implements JsonDeserializer<Gate> {

    private SharedDomainFactory factory;
    
    private final MarkDeserializer markDeserializer;

    public GateDeserializer(SharedDomainFactory factory, MarkDeserializer markDeserializer) {
        this.factory = factory;
        this.markDeserializer = markDeserializer;
    }

    @Override
    public Gate deserialize(JSONObject object) throws JsonDeserializationException {
        
        JSONObject jsonLeftMark = (JSONObject) object.get(GateJsonSerializer.FIELD_LEFT);
        JSONObject jsonRightMark = (JSONObject) object.get(GateJsonSerializer.FIELD_RIGHT);
        Mark leftMark = markDeserializer.deserialize(jsonLeftMark);
        Mark rightMark = markDeserializer.deserialize(jsonRightMark);
        
        String gateName = object.get(BaseControlPointJsonSerializer.FIELD_NAME).toString();
        
        Gate gate = factory.createGate(leftMark, rightMark, gateName);
        return gate;
    }

}