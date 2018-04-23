package com.sap.sailing.gwt.ui.adminconsole.resulthandling;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.sap.sse.common.Util.Triple;

public class ExpeditionDataImportResponse extends AbstractDataImportResponse {

    public static final ExpeditionDataImportResponse parse(String json) {
        return AbstractDataImportResponse.parse(json, "ExpeditionDataImportResponse");
    }

    protected ExpeditionDataImportResponse() {
    }

    public final UUID getEventId() {
        String eventId = getString("eventId");
        return eventId == null ? null : UUID.fromString(eventId);
    }

    public final native String getLeaderboardGroupName() /*-{
        return this.leaderboardGroupName;
    }-*/;

    public final native String getLeaderboardName() /*-{
        return this.leaderboardName;
    }-*/;
    
    public final native String getRegattaName() /*-{
        return this.regattaName;
    }-*/;

    public final List<Triple<String, String, String>> getRaceEntries() {
        ArrayList<Triple<String, String, String>> raceNameRaceColumnNameFleetNameList = new ArrayList<>();
        JSONObject jsonView = new JSONObject(this);
        JSONArray entryList = jsonView.get("raceNameRaceColumnNameFleetNameList").isArray();
        for (int i = 0; i < entryList.size(); i++) {
            JSONObject entry = entryList.get(i).isObject();
            String raceName = entry.get("raceName").isString().stringValue();
            String raceColumnName = entry.get("raceColumnName").isString().stringValue();
            String fleetName = entry.get("fleetName").isString().stringValue();
            raceNameRaceColumnNameFleetNameList
                    .add(new Triple<String, String, String>(raceName, raceColumnName, fleetName));
        }
        return raceNameRaceColumnNameFleetNameList;
    }

    public final List<String> getGpsDeviceIds() {
        return getStringList("gpsDeviceIds");
    }

    public final List<String> getSensorDeviceIds() {
        return getStringList("sensorDeviceIds");
    }

    public final native String getSensorFixImporterType() /*-{
        return this.sensorFixImporterType;
    }-*/;

    public final boolean hasEventId() {
        final String stringValue = getString("eventId");
        return stringValue != null && !stringValue.isEmpty();
    }

}
