package com.sap.sailing.selenium.api.regatta;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.DeviceStatus;
import com.sap.sailing.selenium.api.core.JsonWrapper;

public class RegattaDeviceStatus extends JsonWrapper {
    public abstract static class TrackedItemDeviceStatus extends JsonWrapper {
        public TrackedItemDeviceStatus(JSONObject json) {
            super(json);
        }

        public List<DeviceStatus> getDeviceStatuses() {
            JSONArray statusArray = get("deviceStatuses");
            return statusArray.stream().map(o -> new DeviceStatus((JSONObject) o)).collect(Collectors.toList());
        }
    }

    public static class CompetitorDeviceStatus extends TrackedItemDeviceStatus {
        public CompetitorDeviceStatus(JSONObject json) {
            super(json);
        }

        public String getCompetitorId() {
            return (String) get("competitorId");
        }
    }

    public static class BoatDeviceStatus extends TrackedItemDeviceStatus {
        public BoatDeviceStatus(JSONObject json) {
            super(json);
        }

        public String getBoatId() {
            return (String) get("boatId");
        }
    }

    public static class MarkDeviceStatus extends TrackedItemDeviceStatus {
        public MarkDeviceStatus(JSONObject json) {
            super(json);
        }

        public String getMarkId() {
            return (String) get("markId");
        }
    }

    public RegattaDeviceStatus(JSONObject json) {
        super(json);
    }

    public List<CompetitorDeviceStatus> getCompetitors() {
        Object jsonObjectOrNull = get("competitors");
        return jsonObjectOrNull instanceof JSONArray ? ((JSONArray) jsonObjectOrNull).stream()
                .map(o -> new CompetitorDeviceStatus((JSONObject) o)).collect(Collectors.toList())
                : Collections.emptyList();
    }

    public List<BoatDeviceStatus> getBoats() {
        Object jsonObjectOrNull = get("boats");
        return jsonObjectOrNull instanceof JSONArray ? ((JSONArray) jsonObjectOrNull).stream()
                .map(o -> new BoatDeviceStatus((JSONObject) o)).collect(Collectors.toList()) : Collections.emptyList();
    }

    public List<MarkDeviceStatus> getMarks() {
        Object jsonObjectOrNull = get("marks");
        return jsonObjectOrNull instanceof JSONArray ? ((JSONArray) jsonObjectOrNull).stream()
                .map(o -> new MarkDeviceStatus((JSONObject) o)).collect(Collectors.toList()) : Collections.emptyList();
    }
}