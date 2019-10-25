package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.orc.RaceLogORCSetImpliedWindEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

/**
 * Serializer for {@link com.sap.sailing.domain.abstractlog.orc.RaceLogORCLegDataEvent ORCLegDataEvent}.
 * 
 * @author Axel Uhl (d043530)
 */
public class RaceLogORCSetImpliedWindEventSerializer extends BaseRaceLogEventSerializer {
    public static final String VALUE_CLASS = RaceLogORCSetImpliedWindEvent.class.getSimpleName();
    public static final String ORC_FIXED_IMPLIED_WIND_SPEED_IN_KNOTS = "impliedWindInKnots";

    public RaceLogORCSetImpliedWindEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        final RaceLogORCSetImpliedWindEvent setImpliedWindEvent = (RaceLogORCSetImpliedWindEvent) object;
        final JSONObject result = super.serialize(setImpliedWindEvent);
        result.put(ORC_FIXED_IMPLIED_WIND_SPEED_IN_KNOTS, setImpliedWindEvent.getImpliedWindSpeed() == null ? null : setImpliedWindEvent.getImpliedWindSpeed().getKnots());
        return result;
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }
}
