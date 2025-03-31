package com.sap.sailing.ingestion;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.ingestion.dto.EndpointDTO;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sse.shared.json.JsonDeserializationException;
import com.sap.sse.shared.json.JsonDeserializer;

public class EndpointJsonDeserializer implements JsonDeserializer<EndpointDTO> {
    public static final String ENDPOINT_UUID = "endpointUuid";
    public static final String ACTION = "action";
    public static final String ENDPOINT_CALLBACK_URL = "endpointCallbackUrl";
    public static final String DEVICES_UUID = "devicesUuid";

    @Override
    public EndpointDTO deserialize(JSONObject object) throws JsonDeserializationException {
        final String endpointUuid = String.valueOf(object.get(ENDPOINT_UUID));
        final String action = String.valueOf(object.get(ACTION));
        final String endpointCallbackUrl = String.valueOf(object.get(ENDPOINT_CALLBACK_URL));
        final JSONArray jsonDevices = Helpers.toJSONArraySafe(object.get(DEVICES_UUID));
        final List<String> devicesUuid = new ArrayList<String>();
        for (int i = 0; i < jsonDevices.size(); i++) {
            String deviceUuid = String.valueOf(jsonDevices.get(i));
            devicesUuid.add(deviceUuid);
        }
        EndpointDTO endpoint = new EndpointDTO(endpointUuid, action, endpointCallbackUrl, devicesUuid);
        return endpoint;
    }
}
