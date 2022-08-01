package com.sap.sailing.domain.markpassinghash.impl;

import java.util.Map;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.markpassinghash.impl.TrackedRaceHashForMarkPassingComparatorImpl.TypeOfHash;
import com.sap.sse.shared.json.JsonSerializer;

public class HashForMarkPassingSerializer implements JsonSerializer<Map<TypeOfHash, Integer>> {
    private static final String COMPETITOR = "Competitor";
    private static final String START = "Start";
    private static final String END = "End";
    private static final String WAYYPOINTS = "Waypoints";
    private static final String NUMBEROFGPSFIXES = "NumberOfGPSFixes";
    private static final String GPSFIXES = "GPSFixes";

    @Override
    public JSONObject serialize(Map<TypeOfHash, Integer> hashValues) {
        JSONObject result = new JSONObject();
        result.put(COMPETITOR, hashValues.get(TypeOfHash.COMPETITOR));
        result.put(START, hashValues.get(TypeOfHash.START));
        result.put(END, hashValues.get(TypeOfHash.END));
        result.put(WAYYPOINTS, hashValues.get(TypeOfHash.WAYPOINTS));
        result.put(NUMBEROFGPSFIXES, hashValues.get(TypeOfHash.NUMBEROFGPSFIXES));
        result.put(GPSFIXES, hashValues.get(TypeOfHash.GPSFIXES));

        return result;
    }
}