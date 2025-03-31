package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCImpliedWindSourceEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogExcludeWindSourcesEventImpl;
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
        final JSONArray windSourcesToExcludeJson = (JSONArray) object.get(RaceLogExcludeWindSourceEventSerializer.WIND_SOURCES_TO_EXCLUDE);
        final Set<WindSource> windSourcesToExclude = new HashSet<>();
        for (final Object o : windSourcesToExcludeJson) {
            final JSONObject windSourceToExcludeJson = (JSONObject) o;
            final WindSourceType windSourceType = WindSourceType.valueOf((String) windSourceToExcludeJson.get(RaceLogExcludeWindSourceEventSerializer.WIND_SOURCE_NAME));
            final String windSourceId = (String) windSourceToExcludeJson.get(RaceLogExcludeWindSourceEventSerializer.WIND_SOURCE_ID);
            final WindSource windSourceToExclude;
            if (windSourceId == null) {
                windSourceToExclude = new WindSourceImpl(windSourceType);
            } else {
                windSourceToExclude = new WindSourceWithAdditionalID(windSourceType, windSourceId);
            }
            windSourcesToExclude.add(windSourceToExclude);
        }
        return new RaceLogExcludeWindSourcesEventImpl(createdAt, timePoint, author, id, passId, windSourcesToExclude);
    }

}
