package com.sap.sailing.domain.igtimiadapter.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.igtimiadapter.Permission;
import com.sap.sailing.domain.igtimiadapter.Resource;

public class ResourceDeserializer {
    public Resource createResourceFromJson(JSONObject resourceJson) {
        Boolean blob = (Boolean) resourceJson.get("blob");
        return new ResourceImpl((Long) resourceJson.get("id"),
                new MillisecondsTimePoint(((Double) resourceJson.get("start_time")).longValue()),
                new MillisecondsTimePoint(((Double) resourceJson.get("end_time")).longValue()),
                (String) resourceJson.get("device_serial_number"),
                getDataTypes((JSONArray) resourceJson.get("dadta_types")),
                getPermissions((JSONArray) resourceJson.get("permissions")),
                blob == null ? false : blob);
    }

    private Permission[] getPermissions(JSONArray jsonArray) {
        final Permission[] result = new Permission[jsonArray.size()];
        int i=0;
        for (Object o : jsonArray) {
            result[i++] = Permission.valueOf((String) o);
        }
        return result;
    }

    private int[] getDataTypes(JSONArray jsonArray) {
        final int[] result = new int[jsonArray.size()];
        int i=0;
        for (Object o : jsonArray) {
            result[i++] = (Integer) o;
        }
        return result;
    }
}
