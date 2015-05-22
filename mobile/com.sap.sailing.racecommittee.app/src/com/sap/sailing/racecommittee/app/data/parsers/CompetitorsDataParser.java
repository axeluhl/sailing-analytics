package com.sap.sailing.racecommittee.app.data.parsers;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

public class CompetitorsDataParser implements DataParser<Collection<Competitor>> {

    private JsonDeserializer<Competitor> deserializer;

    public CompetitorsDataParser(JsonDeserializer<Competitor> deserializer) {
        this.deserializer = deserializer;
    }

    public Collection<Competitor> parse(Reader reader) throws Exception {
        Object parsedResult = JSONValue.parseWithException(reader);
        JSONArray jsonArray = Helpers.toJSONArraySafe(parsedResult);
        Collection<Competitor> competitors = new ArrayList<Competitor>();

        for (Object element : jsonArray) {
            JSONObject json = Helpers.toJSONObjectSafe(element);
            Competitor competitor = deserializer.deserialize(json);
            competitors.add(competitor);
        }

        return competitors;
    }

}
