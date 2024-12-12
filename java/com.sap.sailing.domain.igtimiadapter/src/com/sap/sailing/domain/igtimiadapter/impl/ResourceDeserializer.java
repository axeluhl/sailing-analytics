package com.sap.sailing.domain.igtimiadapter.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ResourceDeserializer extends HasPermissionsDeserializer {
    public Resource createResourceFromJson(JSONObject resourceJson, IgtimiConnection conn) {
        return new ResourceImpl((Long) resourceJson.get("id"),
                new MillisecondsTimePoint(((Double) resourceJson.get("start_time")).longValue()),
                new MillisecondsTimePoint(((Double) resourceJson.get("end_time")).longValue()),
                (String) resourceJson.get("device_serial_number"),
                getDataTypes((JSONArray) resourceJson.get("data_types")));
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
