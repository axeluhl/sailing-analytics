package com.sap.sailing.domain.igtimiadapter.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;

public class DeviceDeserializer extends HasPermissionsDeserializer {
    public Device createResourceFromJson(JSONObject sessionJson, IgtimiConnection conn) {
        return new DeviceImpl((Long) sessionJson.get("id"), (String) sessionJson.get("serial_number"), (String) sessionJson.get("name"),
                (String) sessionJson.get("service_tag"));
    }
}
