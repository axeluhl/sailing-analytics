package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogExcludeWindSourceEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sse.shared.json.JsonSerializer;

/**
 * Serializer for {@link com.sap.sailing.domain.abstractlog.orc.RaceLogORCImpliedWindSourceEvent RaceLogORCImpliedWindSourceEvent}.
 * 
 * @author Axel Uhl (d043530)
 */
public class RaceLogExcludeWindSourceEventSerializer extends BaseRaceLogEventSerializer {
    public static final String VALUE_CLASS = RaceLogExcludeWindSourceEvent.class.getSimpleName();
    public static final String WIND_SOURCE_NAME = "windSourceName";
    public static final String WIND_SOURCE_ID = "windSourceId";

    public RaceLogExcludeWindSourceEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        final RaceLogExcludeWindSourceEvent excludeWindSourceEvent = (RaceLogExcludeWindSourceEvent) object;
        final JSONObject result = super.serialize(excludeWindSourceEvent);
        final WindSource windSourceToExclude = excludeWindSourceEvent.getWindSourceToExclude();
        result.put(WIND_SOURCE_NAME, windSourceToExclude.name());
        if (windSourceToExclude.getId() != null) {
            result.put(WIND_SOURCE_ID, windSourceToExclude.getId().toString());
        }
        return result;
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }
}
