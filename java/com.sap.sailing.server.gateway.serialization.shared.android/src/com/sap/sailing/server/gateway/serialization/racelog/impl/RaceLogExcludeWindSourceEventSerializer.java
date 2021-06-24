package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogExcludeWindSourcesEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sse.shared.json.JsonSerializer;

/**
 * Serializer for {@link com.sap.sailing.domain.abstractlog.orc.RaceLogORCImpliedWindSourceEvent RaceLogORCImpliedWindSourceEvent}.
 * 
 * @author Axel Uhl (d043530)
 */
public class RaceLogExcludeWindSourceEventSerializer extends BaseRaceLogEventSerializer {
    public static final String VALUE_CLASS = RaceLogExcludeWindSourcesEvent.class.getSimpleName();
    public static final String WIND_SOURCES_TO_EXCLUDE= "windSourcesToExclude";
    public static final String WIND_SOURCE_NAME = "windSourceName";
    public static final String WIND_SOURCE_ID = "windSourceId";

    public RaceLogExcludeWindSourceEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        final RaceLogExcludeWindSourcesEvent excludeWindSourceEvent = (RaceLogExcludeWindSourcesEvent) object;
        final JSONObject result = super.serialize(excludeWindSourceEvent);
        final JSONArray windSourcesToExclude = new JSONArray();
        result.put(WIND_SOURCES_TO_EXCLUDE, windSourcesToExclude);
        for (final WindSource windSourceToExclude : excludeWindSourceEvent.getWindSourcesToExclude()) {
            final JSONObject windSourceToExcludeJson = new JSONObject();
            windSourceToExcludeJson.put(WIND_SOURCE_NAME, windSourceToExclude.name());
            if (windSourceToExclude.getId() != null) {
                windSourceToExcludeJson.put(WIND_SOURCE_ID, windSourceToExclude.getId().toString());
            }
            windSourcesToExclude.add(windSourceToExcludeJson);
        }
        return result;
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }
}
