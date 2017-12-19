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
        return UUID.fromString(this.eventId());
    }

    private final native String eventId() /*-{
        return this.eventId;
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
        return asList(array("gpsDeviceIds"));
    }
    
    public final List<String> getSensorDeviceIds() {
        return asList(array("sensorDeviceIds"));
    }
    
    private final native String[] array(String fieldName) /*-{
        return this[fieldName];
    }-*/;

    public final native String getSensorFixImporterType() /*-{
        return this.sensorFixImporterType;
    }-*/;

}
