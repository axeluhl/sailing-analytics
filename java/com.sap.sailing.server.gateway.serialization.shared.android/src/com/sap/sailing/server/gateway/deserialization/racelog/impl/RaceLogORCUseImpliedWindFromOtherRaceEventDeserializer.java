package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.orc.impl.RaceLogORCUseImpliedWindFromOtherRaceEventImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.impl.SimpleRaceLogIdentifierImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogORCUseImpliedWindFromOtherRaceEventSerializer;
import com.sap.sse.common.TimePoint;

/**
 * Deserializer for {@link com.sap.sailing.domain.abstractlog.orc.RaceLogORCLegDataEvent ORCLegDataEvent}.
 * 
 * @author Axel Uhl (d043530)
 */
public class RaceLogORCUseImpliedWindFromOtherRaceEventDeserializer extends BaseRaceLogEventDeserializer {

    public RaceLogORCUseImpliedWindFromOtherRaceEventDeserializer(JsonDeserializer<DynamicCompetitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt,
            AbstractLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException {
        final String otherRaceRegattaLikeName = object.get(RaceLogORCUseImpliedWindFromOtherRaceEventSerializer.ORC_OTHER_RACE_REGATTA_LIKE_NAME).toString();
        final String otherRaceRaceColumnName = object.get(RaceLogORCUseImpliedWindFromOtherRaceEventSerializer.ORC_OTHER_RACE_RACE_COLUMN_NAME).toString();
        final String otherRaceFleetName = object.get(RaceLogORCUseImpliedWindFromOtherRaceEventSerializer.ORC_OTHER_RACE_FLEET_NAME).toString();
        final SimpleRaceLogIdentifier otherRace = new SimpleRaceLogIdentifierImpl(otherRaceRegattaLikeName, otherRaceRaceColumnName, otherRaceFleetName);
        return new RaceLogORCUseImpliedWindFromOtherRaceEventImpl(createdAt, timePoint, author, id, passId, otherRace);
    }

}
