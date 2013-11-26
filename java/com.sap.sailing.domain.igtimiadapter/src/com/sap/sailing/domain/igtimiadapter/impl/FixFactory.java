package com.sap.sailing.domain.igtimiadapter.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;

public class FixFactory {
    public Iterable<Fix> createFixes(JSONObject sensorsJson) {
        List<Fix> result = new ArrayList<>();
        for (Entry<Object, Object> e : sensorsJson.entrySet()) {
            String sensorId = (String) e.getKey();
            JSONObject typesJson = (JSONObject) e.getValue();
            for (Entry<Object, Object> fixTypeAndFixesJson : typesJson.entrySet()) {
                int fixType = ((Long) fixTypeAndFixesJson.getKey()).intValue();
                JSONObject fixesJson = (JSONObject) fixTypeAndFixesJson.getValue();
                JSONArray timePointsMillis = (JSONArray) fixesJson.get("t");
                int fixIndex = 0;
                for (Object timePointMillis : timePointsMillis) {
                    TimePoint timePoint = new MillisecondsTimePoint((Long) timePointMillis);
                    Map<Integer, Number> valuesPerSubindex = new HashMap<>();
                    int i=1;
                    JSONArray values;
                    while ((values=(JSONArray) fixesJson.get(""+i)) != null) {
                        valuesPerSubindex.put(i, (Number) values.get(fixIndex));
                    }
                    Fix fix = createFix(sensorId, fixType, timePoint, valuesPerSubindex);
                    result.add(fix);
                }
            }
        }
        return result;
    }

    private Fix createFix(String sensorId, int fixType, TimePoint timePoint, Map<Integer, Number> valuesPerSubindex) {
        // TODO Auto-generated method stub
        return null;
    }
}
