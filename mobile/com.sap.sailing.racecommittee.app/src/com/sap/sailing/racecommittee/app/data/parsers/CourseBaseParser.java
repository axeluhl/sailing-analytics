package com.sap.sailing.racecommittee.app.data.parsers;

import java.io.Reader;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;

public class CourseBaseParser implements DataParser<CourseBase> {

    private JsonDeserializer<CourseBase> deserializer;

    public CourseBaseParser(JsonDeserializer<CourseBase> deserializer) {
        this.deserializer = deserializer;
    }

    @Override
    public CourseBase parse(Reader reader) throws Exception {
        Object parsedResult = JSONValue.parseWithException(reader);
        JSONObject jsonCurrentCourse = Helpers.toJSONObjectSafe(parsedResult);
        CourseBase course = deserializer.deserialize(jsonCurrentCourse);
        return course;
    }

}
