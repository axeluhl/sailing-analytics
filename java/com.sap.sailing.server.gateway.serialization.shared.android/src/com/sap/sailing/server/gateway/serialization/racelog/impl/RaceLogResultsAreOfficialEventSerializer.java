package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogResultsAreOfficialEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

/**
 * Serializer for {@link RaceLogResultsAreOfficialEvent}.
 * 
 * @author Axel Uhl (d043530)
 */
public class RaceLogResultsAreOfficialEventSerializer extends BaseRaceLogEventSerializer {
    public static final String VALUE_CLASS = RaceLogResultsAreOfficialEvent.class.getSimpleName();

    public RaceLogResultsAreOfficialEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        final RaceLogResultsAreOfficialEvent resultsAreOfficialEvent = (RaceLogResultsAreOfficialEvent) object;
        final JSONObject result = super.serialize(resultsAreOfficialEvent);
        return result;
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }
}
