package com.sap.sailing.windestimation.data.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.RaceWindJsonSerializer;
import com.sap.sailing.windestimation.data.WindSourceMetadata;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class WindSourceMetadataJsonDeserializer implements JsonDeserializer<WindSourceMetadata> {

    private final JsonDeserializer<Position> positionDeserializer;

    public WindSourceMetadataJsonDeserializer(JsonDeserializer<Position> positionDeserializer) {
        this.positionDeserializer = positionDeserializer;
    }

    @Override
    public WindSourceMetadata deserialize(JSONObject object) throws JsonDeserializationException {
        Position firstPosition = positionDeserializer
                .deserialize((JSONObject) object.get(RaceWindJsonSerializer.FIRST_POSITION));
        TimePoint startTime = new MillisecondsTimePoint((long) object.get(RaceWindJsonSerializer.START_TIME_POINT));
        TimePoint endTime = new MillisecondsTimePoint((long) object.get(RaceWindJsonSerializer.END_TIME_POINT));
        double samplingRate = (double) object.get(RaceWindJsonSerializer.SAMPLING_RATE);
        return new WindSourceMetadata(firstPosition, startTime, endTime, samplingRate);
    }

}
