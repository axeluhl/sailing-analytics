package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.racelog.impl.BaseRaceLogEventSerializer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.util.impl.UUIDHelper;

public abstract class BaseRaceLogEventDeserializer implements JsonDeserializer<RaceLogEvent> {

    protected JsonDeserializer<DynamicCompetitor> competitorDeserializer;
    
    public BaseRaceLogEventDeserializer(JsonDeserializer<DynamicCompetitor> competitorDeserializer) {
        this.competitorDeserializer = competitorDeserializer;
    }
    
    protected abstract RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, AbstractLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException;

    @Override
    public RaceLogEvent deserialize(JSONObject object) throws JsonDeserializationException {
        // Factory handles class field and subclassing...
        Serializable id = (Serializable) object.get(BaseRaceLogEventSerializer.FIELD_ID);
        Number createdAt = (Number) object.get(BaseRaceLogEventSerializer.FIELD_CREATED_AT);
        Number timeStamp = (Number) object.get(BaseRaceLogEventSerializer.FIELD_TIMESTAMP);
        Number passId = (Number) object.get(BaseRaceLogEventSerializer.FIELD_PASS_ID);
        
        JSONArray jsonCompetitors = Helpers.getNestedArraySafe(object, BaseRaceLogEventSerializer.FIELD_COMPETITORS);
        List<Competitor> competitors = new ArrayList<>();
        for (Object competitorObject : jsonCompetitors) {
            JSONObject jsonCompetitor = (JSONObject) competitorObject;
            DynamicCompetitor competitor = competitorDeserializer.deserialize(jsonCompetitor);
            competitors.add(competitor);
        }
        final String authorName = (String) object.get(BaseRaceLogEventSerializer.FIELD_AUTHOR_NAME);
        final Number authorPriority = (Number) object.get(BaseRaceLogEventSerializer.FIELD_AUTHOR_PRIORITY);
        final AbstractLogEventAuthor author;
        if (authorName != null && authorPriority != null) {
            author = new LogEventAuthorImpl(authorName, authorPriority.intValue());
        } else {
            author = new LogEventAuthorImpl("default", 4);
        }
        return deserialize(
                object, 
                UUIDHelper.tryUuidConversion(id),
                new MillisecondsTimePoint(createdAt.longValue()),
                author, 
                timeStamp == null ? null : new MillisecondsTimePoint(timeStamp.longValue()),
                passId.intValue(), competitors);
    }

}
