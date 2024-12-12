package com.sap.sailing.domain.igtimiadapter.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class DataAccessWindowDeserializer {
    public DataAccessWindow createDataAccessWindowFromJson(JSONObject resourceJson) {
        return new DataAccessWindowImpl((Long) resourceJson.get("id"),
                new MillisecondsTimePoint(((Number) resourceJson.get("start_time")).longValue()),
                new MillisecondsTimePoint(((Number) resourceJson.get("end_time")).longValue()),
                (String) resourceJson.get("device_serial_number"));
    }
}
