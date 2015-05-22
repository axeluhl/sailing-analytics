package com.sap.sailing.racecommittee.app.data.parsers;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

public class MarksDataParser implements DataParser<Collection<Mark>> {

    private JsonDeserializer<Mark> deserializer;

    public MarksDataParser(JsonDeserializer<Mark> deserializer) {
        this.deserializer = deserializer;
    }

    public Collection<Mark> parse(Reader reader) throws Exception {
        Object parsedResult = JSONValue.parseWithException(reader);
        JSONArray jsonArray = Helpers.toJSONArraySafe(parsedResult);
        Collection<Mark> marks = new ArrayList<Mark>();

        for (Object element : jsonArray) {
            JSONObject json = Helpers.toJSONObjectSafe(element);
            Mark mark = deserializer.deserialize(json);
            marks.add(mark);
        }

        return marks;
    }

}
