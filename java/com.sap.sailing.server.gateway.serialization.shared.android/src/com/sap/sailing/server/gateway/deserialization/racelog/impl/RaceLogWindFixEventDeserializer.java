package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogWindFixEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogWindFixEventSerializer;
import com.sap.sse.common.TimePoint;

public class RaceLogWindFixEventDeserializer extends BaseRaceLogEventDeserializer {
    
    private final JsonDeserializer<Wind> windDeserializer;

    public RaceLogWindFixEventDeserializer(JsonDeserializer<DynamicCompetitor> competitorDeserializer, JsonDeserializer<Wind> windDeserializer) {
        super(competitorDeserializer);
        this.windDeserializer = windDeserializer;
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint timePoint, int passId, List<Competitor> competitors) throws JsonDeserializationException {
        final JSONObject windJsonObject = Helpers.getNestedObjectSafe(object, RaceLogWindFixEventSerializer.FIELD_WIND);
        final Wind wind = windDeserializer.deserialize(windJsonObject);
        final Boolean isMagnetic = (Boolean) object.get(RaceLogWindFixEventSerializer.FIELD_MAGNETIC);
        return new RaceLogWindFixEventImpl(createdAt, timePoint, author, id, passId, wind, isMagnetic == null ? true : isMagnetic);
    }

}
