package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogFlagEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogFlagEventSerializer;
import com.sap.sse.common.TimePoint;

public class RaceLogFlagEventDeserializer extends BaseRaceLogEventDeserializer {
    
    public RaceLogFlagEventDeserializer(JsonDeserializer<DynamicCompetitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, AbstractLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors) {

        Flags upperFlag = Flags.valueOf(object.get(RaceLogFlagEventSerializer.FIELD_UPPER_FLAG).toString());
        Flags lowerFlag = Flags.valueOf(object.get(RaceLogFlagEventSerializer.FIELD_LOWER_FLAG).toString());
        boolean isDisplayed = (Boolean) object.get(RaceLogFlagEventSerializer.FIELD_DISPLAYED);

        return new RaceLogFlagEventImpl(createdAt, timePoint, author, id, passId, upperFlag, lowerFlag, isDisplayed);
    }

}
