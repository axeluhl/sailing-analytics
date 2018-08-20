package com.sap.sailing.server.gateway.serialization.racegroup.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.base.racegroup.RaceCell;
import com.sap.sailing.domain.common.TargetTimeInfo;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceCellJsonSerializer implements JsonSerializer<RaceCell> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_RACE_LOG = "raceLog";
    public static final String FIELD_COMPETITORS = "competitors";
    public static final String FIELD_FACTOR = "factor";
    public static final String FIELD_EXPLICIT_FACTOR = "explicitfactor";
    public static final String FIELD_ZERO_BASED_INDEX_IN_FLEET = "zerobasedindexinfleet";
    public static final String FIELD_TARGET_TIME_INFO = "targettimeinfo";
    
    private final JsonSerializer<RaceLog> logSerializer;
    private final JsonSerializer<TargetTimeInfo> targetTimeInfoSerializer;

    public RaceCellJsonSerializer(JsonSerializer<RaceLog> logSerializer, JsonSerializer<TargetTimeInfo> targetTimeInfoSerializer) {
        this.logSerializer = logSerializer;
        this.targetTimeInfoSerializer = targetTimeInfoSerializer;
    }

    @Override
    public JSONObject serialize(RaceCell object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, object.getName());
        result.put(FIELD_FACTOR, object.getFactor());
        result.put(FIELD_EXPLICIT_FACTOR, object.getExplicitFactor());
        result.put(FIELD_ZERO_BASED_INDEX_IN_FLEET, object.getZeroBasedIndexInFleet());
        result.put(FIELD_RACE_LOG, logSerializer.serialize(object.getRaceLog()));
        final TargetTimeInfo targetTime = object.getTargetTime();
        result.put(FIELD_TARGET_TIME_INFO, targetTime==null?null:targetTimeInfoSerializer.serialize(targetTime));
        return result;
    }

}
