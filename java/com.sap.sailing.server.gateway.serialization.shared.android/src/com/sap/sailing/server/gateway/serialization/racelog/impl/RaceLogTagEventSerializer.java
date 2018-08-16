package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogTagEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogTagEventSerializer extends BaseRaceLogEventSerializer {

    public static final String VALUE_CLASS = RaceLogTagEvent.class.getSimpleName();
    public static final String FIELD_TAG = "tag";
    public static final String FIELD_COMMENT = "comment";
    public static final String FIELD_URL = "url";
    public static final String FIELD_IS_VISIBLE_FOR_PUBLIC = "ispublic";

    public RaceLogTagEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogTagEvent tagEvent = (RaceLogTagEvent) object;
        JSONObject result = super.serialize(tagEvent);
        result.put(FIELD_TAG, tagEvent.getTag());
        result.put(FIELD_COMMENT, tagEvent.getComment());
        result.put(FIELD_URL, tagEvent.getImageURL());
        result.put(FIELD_IS_VISIBLE_FOR_PUBLIC, tagEvent.isVisibleForPublic());
        return result;
    }
}