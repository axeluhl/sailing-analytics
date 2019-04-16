package com.sap.sailing.selenium.api.event;

import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.core.JsonWrapper;

public class GPSFixApi {

    private static final String POST_FIX_URL = "/api/v1/gps_fixes";

    public GpsFixResponse postGpsFix(ApiContext ctx, UUID deviceUuid, GpsFix... fixes) {
        JSONObject gpsFix = new JSONObject();
        gpsFix.put("deviceUuid", deviceUuid != null ? deviceUuid.toString(): null);
        JSONArray fixesArray = new JSONArray();
        gpsFix.put("fixes", fixesArray);
        GpsFixResponse gpsFixResponse = new GpsFixResponse(ctx.post(POST_FIX_URL, null, gpsFix));
        return gpsFixResponse;
    }

    public class GpsFixResponse extends JsonWrapper {

        public GpsFixResponse(JSONObject json) {
            super(json);
        }

        public ManeuverChanged[] getManeuverChanged() {
            return get("maneuverchanged");
        }
    }

    public static JSONObject createGpsFixJson(Double longitude, Double latitude, Long timestamp, Double speed,
            Double course) {
        JSONObject json = new JSONObject();
        json.put("longitude", longitude);
        json.put("latitude", latitude);
        json.put("timestamp", timestamp);
        json.put("speed", speed);
        json.put("course", course);
        return json;
    }

    public class GpsFix extends JsonWrapper {

        public GpsFix(JSONObject json) {
            super(json);
        }

        public GpsFix(Double longitude, Double latitude, Long timeMillis, Double speed, Double course) {
            super(createGpsFixJson(longitude, latitude, timeMillis, speed, course));
        }

        public Double getLongitude() {
            return get("longitude");
        }

        public Double getLatitude() {
            return get("latitude");
        }

        public Long getTimestamp() {
            return get("timestamp");
        }

        public Double getSpeed() {
            return get("speed");
        }

        public Double getCourse() {
            return get("course");
        }
    }

    public class ManeuverChanged extends JsonWrapper {

        public ManeuverChanged(JSONObject json) {
            super(json);
        }

        public String getRegattaName() {
            return get("regattaName");
        }

        public String getRaceName() {
            return get("raceName");
        }
    }
}
