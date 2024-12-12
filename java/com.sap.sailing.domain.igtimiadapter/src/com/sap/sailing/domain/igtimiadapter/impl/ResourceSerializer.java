package com.sap.sailing.domain.igtimiadapter.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sailing.domain.igtimiadapter.datatypes.Type;

public class ResourceSerializer {
    public JSONObject createJsonFromResource(Resource resource) {
        final JSONObject resourceJson = new JSONObject();
        resourceJson.put(ResourceDeserializer.ID, resource.getId());
        resourceJson.put(ResourceDeserializer.START_TIME, resource.getStartTime()==null?null:resource.getStartTime().asMillis());
        resourceJson.put(ResourceDeserializer.END_TIME, resource.getEndTime()==null?null:resource.getEndTime().asMillis());
        resourceJson.put(ResourceDeserializer.DEVICE_SERIAL_NUMBER, resource.getDeviceSerialNumber());
        final JSONArray dataTypesJson = new JSONArray();
        for (final Type dataType : resource.getDataTypes()) {
            dataTypesJson.add(dataType.getCode());
        }
        resourceJson.put(ResourceDeserializer.DATA_TYPES, dataTypesJson);
        return resourceJson;
    }
}
