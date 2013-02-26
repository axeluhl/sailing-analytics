package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.util.Collections;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.impl.RaceLogFlagEventImpl;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogFlagEventSerializer;

public class RaceLogFlagEventDeserializer extends BaseRaceLogEventDeserializer {

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint timePoint, int passId) {

        Flags upperFlag = Flags.valueOf(object.get(RaceLogFlagEventSerializer.FIELD_UPPER_FLAG).toString());
        Flags lowerFlag = Flags.valueOf(object.get(RaceLogFlagEventSerializer.FIELD_LOWER_FLAG).toString());
        boolean isDisplayed = (Boolean) object.get(RaceLogFlagEventSerializer.FIELD_DISPLAYED);

        return new RaceLogFlagEventImpl(timePoint, id, Collections.<Competitor> emptyList(), passId, upperFlag,
                lowerFlag, isDisplayed);
    }

}
