package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.base.impl.VenueImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.VenueJsonSerializer;

public class VenueJsonDeserializer implements JsonDeserializer<Venue> {
    private JsonDeserializer<CourseArea> courseAreaDeserializer;

    public VenueJsonDeserializer(JsonDeserializer<CourseArea> courseAreaDeserializer) {
        this.courseAreaDeserializer = courseAreaDeserializer;
    }

    public Venue deserialize(JSONObject object) throws JsonDeserializationException {
        String name = (String) object.get(VenueJsonSerializer.FIELD_NAME);
        Venue venue = new VenueImpl(name);

        JSONArray courseAreaArray = Helpers.getNestedArraySafe(object, VenueJsonSerializer.FIELD_COURSE_AREAS);
        for (Object element : courseAreaArray) {
            JSONObject courseAreaObject = Helpers.toJSONObjectSafe(element);
            venue.addCourseArea(courseAreaDeserializer.deserialize(courseAreaObject));
        }
        return venue;
    }
}
