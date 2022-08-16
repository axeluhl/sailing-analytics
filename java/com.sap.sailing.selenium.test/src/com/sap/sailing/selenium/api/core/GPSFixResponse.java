package com.sap.sailing.selenium.api.core;

import org.json.simple.JSONObject;

public class GPSFixResponse extends JsonWrapper {
    public GPSFixResponse(JSONObject json) {
        super(json);
    }
    
    public long getTime() {
        return ((Number)get("unixtime")).longValue();
    }
}