package com.sap.sailing.gwt.ui.adminconsole.resulthandling;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.sap.sailing.domain.common.dto.ExpeditionAllInOneConstants;
import com.sap.sailing.domain.common.dto.ImportConstants;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ExpeditionDataImportResponse extends AbstractDataImportResponse {

    public static final ExpeditionDataImportResponse parse(String json) {
        return AbstractDataImportResponse.parse(json, "ExpeditionDataImportResponse");
    }

    protected ExpeditionDataImportResponse() {
    }

    public final UUID getEventId() {
        String eventId = getString(ExpeditionAllInOneConstants.RESPONSE_EVENT_ID);
        return eventId == null ? null : UUID.fromString(eventId);
    }

    public final String getLeaderboardGroupName() {
        return getString(ExpeditionAllInOneConstants.RESPONSE_LEADER_BOARD_GROUP_NAME);
    }
    
    public final String getLeaderboardName() {
        return getString(ExpeditionAllInOneConstants.RESPONSE_LEADER_BOARD_NAME);
    }
    
    public final String getRegattaName() {
        return getString(ExpeditionAllInOneConstants.RESPONSE_REGATTA_NAME);
    }
    
    public final List<Triple<String, String, String>> getRaceEntries() {
        ArrayList<Triple<String, String, String>> raceNameRaceColumnNameFleetNameList = new ArrayList<>();
        JSONObject jsonView = new JSONObject(this);
        JSONArray entryList = jsonView.get(ExpeditionAllInOneConstants.RESPONSE_RACE_LIST).isArray();
        for (int i = 0; i < entryList.size(); i++) {
            JSONObject entry = entryList.get(i).isObject();
            String raceName = entry.get(ImportConstants.RACE_NAME).isString().stringValue();
            String raceColumnName = entry.get(ImportConstants.RACE_COLUMN_NAME).isString().stringValue();
            String fleetName = entry.get(ImportConstants.FLEET_NAME).isString().stringValue();
            raceNameRaceColumnNameFleetNameList
                    .add(new Triple<String, String, String>(raceName, raceColumnName, fleetName));
        }
        return raceNameRaceColumnNameFleetNameList;
    }
    
    public final Iterable<TimePoint> getStartTimes() {
        final List<TimePoint> result = new ArrayList<>();
        JSONObject jsonView = new JSONObject(this);
        JSONArray startTimesList = jsonView.get(ExpeditionAllInOneConstants.RESPONSE_START_TIMES).isArray();
        for (int i = 0; i < startTimesList.size(); i++) {
            final long startTimeAsMillis = (long) startTimesList.get(i).isNumber().doubleValue();
            result.add(new MillisecondsTimePoint(startTimeAsMillis));
        }
        return result;
    }

    public final List<String> getGpsDeviceIds() {
        return getStringList(ExpeditionAllInOneConstants.RESPONSE_GPS_DEVICE_IDS);
    }

    public final List<String> getSensorDeviceIds() {
        return getStringList(ExpeditionAllInOneConstants.RESPONSE_SENSOR_DEVICE_IDS);
    }

    public final String getSensorFixImporterType() {
        return getString(ExpeditionAllInOneConstants.RESPONSE_SENSOR_FIX_IMPORTER_TYPE);
    };

    public final boolean hasEventId() {
        final String stringValue = getString("eventId");
        return stringValue != null && !stringValue.isEmpty();
    }

}
