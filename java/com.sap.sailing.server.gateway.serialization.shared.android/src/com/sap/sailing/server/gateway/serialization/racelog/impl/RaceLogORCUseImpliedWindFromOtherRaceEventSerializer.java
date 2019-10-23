package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.orc.RaceLogORCUseImpliedWindFromOtherRaceEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

/**
 * Serializer for {@link com.sap.sailing.domain.abstractlog.orc.RaceLogORCLegDataEvent ORCLegDataEvent}.
 * 
 * @author Daniel Lisunkin (i505543)
 * @author Axel Uhl (d043530)
 */
public class RaceLogORCUseImpliedWindFromOtherRaceEventSerializer extends BaseRaceLogEventSerializer {
    public static final String VALUE_CLASS = RaceLogORCUseImpliedWindFromOtherRaceEvent.class.getSimpleName();
    public static final String ORC_OTHER_RACE_REGATTA_LIKE_NAME = "regatta_like";
    public static final String ORC_OTHER_RACE_RACE_COLUMN_NAME = "race_column";
    public static final String ORC_OTHER_RACE_FLEET_NAME = "fleet";

    public RaceLogORCUseImpliedWindFromOtherRaceEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        final RaceLogORCUseImpliedWindFromOtherRaceEvent useImpliedWindFromOtherRaceEvent = (RaceLogORCUseImpliedWindFromOtherRaceEvent) object;
        final JSONObject result = super.serialize(useImpliedWindFromOtherRaceEvent);
        result.put(ORC_OTHER_RACE_REGATTA_LIKE_NAME, useImpliedWindFromOtherRaceEvent.getOtherRace().getRegattaLikeParentName());
        result.put(ORC_OTHER_RACE_RACE_COLUMN_NAME, useImpliedWindFromOtherRaceEvent.getOtherRace().getRaceColumnName());
        result.put(ORC_OTHER_RACE_FLEET_NAME, useImpliedWindFromOtherRaceEvent.getOtherRace().getFleetName());
        return result;
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }
}
