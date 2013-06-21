package com.sap.sailing.server.gateway.deserialization.masterdata.impl;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.impl.EventMasterData;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.EventMasterDataJsonSerializer;

public class EventMasterDataJsonDeserializer implements JsonDeserializer<EventMasterData> {

    @Override
    public EventMasterData deserialize(JSONObject object) throws JsonDeserializationException {
        String id = (String) object.get(EventMasterDataJsonSerializer.FIELD_ID);
        String name = (String) object.get(EventMasterDataJsonSerializer.FIELD_NAME);
        String venueName = (String) object.get(EventMasterDataJsonSerializer.FIELD_VENUE_NAME);
        String pubUrl = (String) object.get(EventMasterDataJsonSerializer.FIELD_PUB_URL);
        boolean isPublic = (Boolean) object.get(EventMasterDataJsonSerializer.FIELD_IS_PUBLIC);
        Map<String,String> courseAreas = deserializeCourseAreas((JSONArray) object.get(EventMasterDataJsonSerializer.FIELD_COURSE_AREAS));
        return new EventMasterData(id, name, venueName, pubUrl, courseAreas, isPublic);
    }

    private Map<String, String> deserializeCourseAreas(JSONArray jsonArray) {
        Map<String,String> courseAreas = new HashMap<String, String>();
        for (Object courseObject : jsonArray) {
            JSONObject courseJson = (JSONObject) courseObject;
            String id = (String) courseJson.get(EventMasterDataJsonSerializer.FIELD_ID);
            String name = (String) courseJson.get(EventMasterDataJsonSerializer.FIELD_NAME);
            courseAreas.put(id, name);
        }
        
        return courseAreas;
    }

}
