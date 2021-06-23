package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCImpliedWindSourceEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogExcludeWindSourceEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogExcludeWindSourceEventSerializer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.shared.json.JsonDeserializationException;
import com.sap.sse.shared.json.JsonDeserializer;

/**
 * Deserializer for {@link RaceLogORCImpliedWindSourceEvent}.
 * 
 * @author Axel Uhl (d043530)
 */
public class RaceLogExcludeWindSourceEventDeserializer extends BaseRaceLogEventDeserializer {
    public RaceLogExcludeWindSourceEventDeserializer(JsonDeserializer<DynamicCompetitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt,
            AbstractLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException {
        final WindSourceType windSourceType = WindSourceType.valueOf((String) object.get(RaceLogExcludeWindSourceEventSerializer.WIND_SOURCE_NAME));
        final String windSourceId = (String) object.get(RaceLogExcludeWindSourceEventSerializer.WIND_SOURCE_ID);
        final RaceLogEvent result;
        final WindSource windSourceToExclude;
        if (windSourceId == null) {
            windSourceToExclude = new WindSourceImpl(windSourceType);
        } else {
            windSourceToExclude = new WindSourceWithAdditionalID(windSourceType, windSourceId);
        }
        result = new RaceLogExcludeWindSourceEventImpl(createdAt, timePoint, author, id, passId, windSourceToExclude);
        return result;
    }

}
