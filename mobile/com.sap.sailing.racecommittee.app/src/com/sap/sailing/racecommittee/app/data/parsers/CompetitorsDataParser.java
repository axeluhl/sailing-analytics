package com.sap.sailing.racecommittee.app.data.parsers;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.server.gateway.deserialization.impl.CompetitorAndBoatJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sse.common.Util.Pair;

public class CompetitorsDataParser implements DataParser<Map<Competitor, Boat>> {

    private CompetitorAndBoatJsonDeserializer deserializer;

    public CompetitorsDataParser(CompetitorAndBoatJsonDeserializer deserializer) {
        this.deserializer = deserializer;
    }

    public Map<Competitor, Boat> parse(Reader reader) throws Exception {
        Object parsedResult = JSONValue.parseWithException(reader);
        JSONArray jsonArray = Helpers.toJSONArraySafe(parsedResult);
        Map<Competitor, Boat> competitorsAndBoats = new HashMap<>();
        for (Object element : jsonArray) {
            JSONObject json = Helpers.toJSONObjectSafe(element);
            Pair<DynamicCompetitor, Boat> competitorAndBoatPair = deserializer.deserialize(json);
            competitorsAndBoats.put(competitorAndBoatPair.getA(), competitorAndBoatPair.getB());
        }
        return competitorsAndBoats;
    }

}
