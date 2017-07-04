package com.sap.sailing.server.gateway.deserialization.racegroup.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.base.racegroup.RaceCell;
import com.sap.sailing.domain.base.racegroup.impl.RaceCellImpl;
import com.sap.sailing.domain.common.TargetTimeInfo;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.racegroup.impl.RaceCellJsonSerializer;

public class RaceCellDeserializer implements JsonDeserializer<RaceCell> {

    private final JsonDeserializer<RaceLog> logDeserializer;
    private final JsonDeserializer<TargetTimeInfo> targetTimeDeserializer;

    public RaceCellDeserializer(JsonDeserializer<RaceLog> logDeserializer, JsonDeserializer<TargetTimeInfo> targetTimeDeserializer) {
        this.logDeserializer = logDeserializer;
        this.targetTimeDeserializer = targetTimeDeserializer;
    }

    public RaceCell deserialize(JSONObject object)
            throws JsonDeserializationException {
        final String name = object.get(RaceCellJsonSerializer.FIELD_NAME).toString();
        final Object factorJson = object.get(RaceCellJsonSerializer.FIELD_FACTOR);
        final double factor = factorJson == null ? 1 : ((Number) factorJson).doubleValue();
        final Double explicitFactor = (Double) object.get(RaceCellJsonSerializer.FIELD_EXPLICIT_FACTOR);
        final Number zeroBasedIndexInFleet = (Number) object.get(RaceCellJsonSerializer.FIELD_ZERO_BASED_INDEX_IN_FLEET);
        final JSONObject targetTimeInfoJson = (JSONObject) object.get(RaceCellJsonSerializer.FIELD_TARGET_TIME_INFO);
        final TargetTimeInfo targetTime = targetTimeInfoJson == null ? null : targetTimeDeserializer.deserialize(targetTimeInfoJson);
        JSONObject logJson = Helpers.getNestedObjectSafe(object, RaceCellJsonSerializer.FIELD_RACE_LOG);
        RaceLog log = logDeserializer.deserialize(logJson);
        return new RaceCellImpl(name, log, factor, explicitFactor, zeroBasedIndexInFleet == null ? -1 : zeroBasedIndexInFleet.intValue(), targetTime);
    }

}
