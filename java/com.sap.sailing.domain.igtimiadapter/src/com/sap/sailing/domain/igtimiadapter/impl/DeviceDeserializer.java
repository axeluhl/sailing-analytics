package com.sap.sailing.domain.igtimiadapter.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.igtimiadapter.Device;

public class DeviceDeserializer {
    static final String ID = "id";
    static final String SERIAL_NUMBER = "serial_number";
    static final String NAME = "name";
    static final String SERVICE_TAG = "service_tag";

    public Device createDeviceFromJson(JSONObject sessionJson) {
        return new DeviceImpl((Long) sessionJson.get(ID), (String) sessionJson.get(SERIAL_NUMBER),
                (String) sessionJson.get(NAME), (String) sessionJson.get(SERVICE_TAG));
    }
}
