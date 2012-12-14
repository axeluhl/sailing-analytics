package com.sap.sailing.domain.swisstimingreplayadapter.impl;

public class SwissTimingRaceConfig {

    public final String latitude;
    public final String longitude;
    public final String country_code;
    public final String gmt_offset;
    public final String location;
    public final String event_name;
    public final String race_start_ts;

    public SwissTimingRaceConfig(String latitude, String longitude, String country_code, String gmt_offset,
            String location, String event_name, String race_start_ts) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.country_code = country_code;
        this.gmt_offset = gmt_offset;
        this.location = location;
        this.event_name = event_name;
        this.race_start_ts = race_start_ts;
    }

}
