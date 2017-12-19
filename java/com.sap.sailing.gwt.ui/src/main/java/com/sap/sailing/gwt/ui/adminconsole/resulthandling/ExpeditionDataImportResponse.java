package com.sap.sailing.gwt.ui.adminconsole.resulthandling;

import java.util.List;
import java.util.UUID;

public class ExpeditionDataImportResponse extends AbstractDataImportResponse {

    public static final ExpeditionDataImportResponse parse(String json) {
        return AbstractDataImportResponse.parse(json, "ExpeditionDataImportResponse");
    }

    protected ExpeditionDataImportResponse() {
    }
    
    public final UUID getEventId() {
        return UUID.fromString(getString("eventId"));
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

    public final native String getRaceName() /*-{
        return this.raceName;
    }-*/;

    public final native String getRaceColumnName() /*-{
        return this.raceColumnName;
    }-*/;

    public final native String getFleetName() /*-{
        return this.fleetName;
    }-*/;
    
    public final List<String> getGpsDeviceIds() {
        return getStringList("gpsDeviceIds");
    }
    
    public final List<String> getSensorDeviceIds() {
        return getStringList("sensorDeviceIds");
    }
    
    public final native String getSensorFixImporterType() /*-{
        return this.sensorFixImporterType;
    }-*/;

}
