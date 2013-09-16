package com.sap.sailing.server.gateway.deserialization.masterdata.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.masterdataimport.EventMasterData;
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
        Iterable<Pair<String, String>> courseAreas = deserializeCourseAreas((JSONArray) object.get(EventMasterDataJsonSerializer.FIELD_COURSE_AREAS));
        return new EventMasterData(id, name, venueName, pubUrl, courseAreas, isPublic);
    }

    private Iterable<Pair<String, String>> deserializeCourseAreas(JSONArray jsonArray) {
        List<Pair<String, String>> courseAreas = new ArrayList<Pair<String,String>>();
        for (Object courseObject : jsonArray) {
            JSONObject courseJson = (JSONObject) courseObject;
            String id = (String) courseJson.get(EventMasterDataJsonSerializer.FIELD_ID);
            String name = (String) courseJson.get(EventMasterDataJsonSerializer.FIELD_NAME);
            courseAreas.add(new Pair<String, String>(id, name));
        }
        Collections.sort(courseAreas, new Comparator<Pair<String,String>>() {
            @Override
            public int compare(Pair<String, String> courseArea1, Pair<String, String> courseArea2) {
                return courseArea1.getB().compareTo(courseArea2.getB());
            }
        });
        return courseAreas;
    }

}
