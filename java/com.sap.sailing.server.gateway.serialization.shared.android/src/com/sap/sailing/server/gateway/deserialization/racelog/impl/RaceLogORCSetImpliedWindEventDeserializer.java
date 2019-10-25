package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCSetImpliedWindEvent;
import com.sap.sailing.domain.abstractlog.orc.impl.RaceLogORCSetImpliedWindEventImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogORCSetImpliedWindEventSerializer;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;

/**
 * Deserializer for {@link RaceLogORCSetImpliedWindEvent}.
 * 
 * @author Axel Uhl (d043530)
 */
public class RaceLogORCSetImpliedWindEventDeserializer extends BaseRaceLogEventDeserializer {

    public RaceLogORCSetImpliedWindEventDeserializer(JsonDeserializer<DynamicCompetitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt,
            AbstractLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException {
        final Number impliedWindSpeedInKnotsAsNumber = (Number) object.get(RaceLogORCSetImpliedWindEventSerializer.ORC_FIXED_IMPLIED_WIND_SPEED_IN_KNOTS);
        final Speed impliedWindSpeed = impliedWindSpeedInKnotsAsNumber == null ? null : new KnotSpeedImpl(impliedWindSpeedInKnotsAsNumber.doubleValue());
        return new RaceLogORCSetImpliedWindEventImpl(createdAt, timePoint, author, id, passId, impliedWindSpeed);
    }

}
