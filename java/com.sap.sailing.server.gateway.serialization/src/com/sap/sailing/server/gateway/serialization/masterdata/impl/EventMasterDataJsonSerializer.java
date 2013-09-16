package com.sap.sailing.server.gateway.serialization.masterdata.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class EventMasterDataJsonSerializer implements JsonSerializer<Event> {

    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_VENUE_NAME = "venueName";
    public static final String FIELD_PUB_URL = "publicationUrl";
    public static final String FIELD_COURSE_AREAS = "courseAreas";
    public static final String FIELD_IS_PUBLIC = "isPublic";
    

    @Override
    public JSONObject serialize(Event event) {
        JSONObject result = new JSONObject();
        result.put(FIELD_ID, event.getId().toString());
        result.put(FIELD_NAME, event.getName());
        result.put(FIELD_VENUE_NAME, event.getVenue().getName());
        result.put(FIELD_PUB_URL, event.getPublicationUrl());
        result.put(FIELD_IS_PUBLIC, event.isPublic());
        result.put(FIELD_COURSE_AREAS, createJsonArrayForCourseAreas(event.getVenue().getCourseAreas()));
        //TODO Sadly event.getRegattas() returns an empty collection
        //result.put(FIELD_REGATTAS, createJsonArrayForRegattas(event.getRegattas()));
        return result;
    }

    private JSONArray createJsonArrayForCourseAreas(Iterable<CourseArea> courseAreas) {
        JSONArray array = new JSONArray();
        for (CourseArea courseArea : courseAreas) {
            array.add(createJsonForCourseArea(courseArea));
        }
        return array;
    }

    private JSONObject createJsonForCourseArea(CourseArea courseArea) {
        JSONObject result = new JSONObject();
        result.put(FIELD_ID, courseArea.getId().toString());
        result.put(FIELD_NAME, courseArea.getName());
        return result;
    }

}
