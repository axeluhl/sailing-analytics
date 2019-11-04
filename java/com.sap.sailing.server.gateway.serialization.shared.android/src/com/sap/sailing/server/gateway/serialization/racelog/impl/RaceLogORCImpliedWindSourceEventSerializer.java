package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.orc.RaceLogORCImpliedWindSourceEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.orc.ImpliedWindSource;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

/**
 * Serializer for {@link com.sap.sailing.domain.abstractlog.orc.RaceLogORCImpliedWindSourceEvent RaceLogORCImpliedWindSourceEvent}.
 * 
 * @author Axel Uhl (d043530)
 */
public class RaceLogORCImpliedWindSourceEventSerializer extends BaseRaceLogEventSerializer {
    public static final String VALUE_CLASS = RaceLogORCImpliedWindSourceEvent.class.getSimpleName();
    public static final String ORC_IMPLIED_WIND_SOURCE = "impliedWindSource";

    public RaceLogORCImpliedWindSourceEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        final RaceLogORCImpliedWindSourceEvent setImpliedWindEvent = (RaceLogORCImpliedWindSourceEvent) object;
        final JSONObject result = super.serialize(setImpliedWindEvent);
        final ImpliedWindSource impliedWindSource = setImpliedWindEvent.getImpliedWindSource();
        final JSONObject impliedWindSourceAsJson = new ImpliedWindSourceSerializer().serialize(impliedWindSource);
        result.put(ORC_IMPLIED_WIND_SOURCE, impliedWindSourceAsJson);
        return result;
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }
}
