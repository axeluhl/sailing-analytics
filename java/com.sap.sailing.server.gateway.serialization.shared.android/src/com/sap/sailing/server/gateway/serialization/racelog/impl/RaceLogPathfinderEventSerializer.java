package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPathfinderEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogPathfinderEventSerializer extends BaseRaceLogEventSerializer {

    public static final String VALUE_CLASS = RaceLogPathfinderEvent.class.getSimpleName();
    public static final String FIELD_PATHFINDER_ID = "pathfinderId";

    public RaceLogPathfinderEventSerializer(
            JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogPathfinderEvent pathfinderEvent = (RaceLogPathfinderEvent) object;

        JSONObject result = super.serialize(pathfinderEvent);
        result.put(FIELD_PATHFINDER_ID, pathfinderEvent.getPathfinderId());

        return result;
    }

}
