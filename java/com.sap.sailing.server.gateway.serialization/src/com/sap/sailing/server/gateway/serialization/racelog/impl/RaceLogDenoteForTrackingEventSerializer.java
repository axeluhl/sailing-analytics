package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DenoteForTrackingEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogDenoteForTrackingEventSerializer extends BaseRaceLogEventSerializer {
    public static final String VALUE_CLASS = DenoteForTrackingEvent.class.getSimpleName();
    public static final String FIELD_RACE_NAME = "raceName";
    public static final String FIELD_BOAT_CLASS = "boatClass";
    public static final String FIELD_RACE_ID = "raceId";

    public RaceLogDenoteForTrackingEventSerializer(
            JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        DenoteForTrackingEvent event = (DenoteForTrackingEvent) object;

        JSONObject result = super.serialize(event);
        result.put(FIELD_RACE_NAME, event.getRaceName());
        result.put(FIELD_BOAT_CLASS, event.getBoatClass().getName());
        result.put(FIELD_RACE_ID, event.getRaceId().toString());

        return result;
    }

}
