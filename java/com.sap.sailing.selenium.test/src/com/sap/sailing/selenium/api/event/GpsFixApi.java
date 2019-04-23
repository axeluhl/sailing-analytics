package com.sap.sailing.selenium.api.event;

import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.core.GpsFix;
import com.sap.sailing.selenium.api.core.JsonWrapper;

public class GpsFixApi {

    private static final String POST_FIX_URL = "/api/v1/gps_fixes";

    public GpsFixResponse postGpsFix(final ApiContext ctx, final UUID deviceUuid, final GpsFix... fixes) {
        final JSONObject gpsFix = new JSONObject();
        gpsFix.put("deviceUuid", deviceUuid != null ? deviceUuid.toString(): null);
        final JSONArray fixesArray = new JSONArray();
        gpsFix.put("fixes", fixesArray);
        return new GpsFixResponse(ctx.post(POST_FIX_URL, null, gpsFix));
    }

    public class GpsFixResponse extends JsonWrapper {

        public GpsFixResponse(JSONObject json) {
            super(json);
        }

        public ManeuverChanged[] getManeuverChanged() {
            return get("maneuverchanged");
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
