package com.sap.sailing.selenium.api.regatta;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.JsonWrapper;

public class RaceColumn extends JsonWrapper {

    public RaceColumn(JSONObject json) {
        super(json);
    }

    public String getSeriesName() {
        return get("seriesname");
    }

    public String getRaceName() {
        return get("racename");
    }
}