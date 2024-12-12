package com.sap.sailing.domain.igtimiadapter.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ResourceDeserializer {
    static final String ID = "id";
    static final String START_TIME = "start_time";
    static final String END_TIME = "end_time";
    static final String DEVICE_SERIAL_NUMBER = "device_serial_number";
    static final String DATA_TYPES = "data_types";

    public Resource createResourceFromJson(JSONObject resourceJson) {
        return new ResourceImpl((Long) resourceJson.get(ID),
                new MillisecondsTimePoint(((Double) resourceJson.get(START_TIME)).longValue()),
                new MillisecondsTimePoint(((Double) resourceJson.get(END_TIME)).longValue()),
                (String) resourceJson.get(DEVICE_SERIAL_NUMBER),
                getDataTypes((JSONArray) resourceJson.get(DATA_TYPES)));
    }

    private int[] getDataTypes(JSONArray jsonArray) {
        final int[] result = new int[jsonArray.size()];
        int i=0;
        for (Object o : jsonArray) {
            result[i++] = ((Long) o).intValue();
        }
        return result;
    }
}
