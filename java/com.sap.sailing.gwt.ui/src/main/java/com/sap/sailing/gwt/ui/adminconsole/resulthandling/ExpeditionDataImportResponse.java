package com.sap.sailing.gwt.ui.adminconsole.resulthandling;

import java.util.UUID;

public class ExpeditionDataImportResponse extends AbstractDataImportResponse {

    public static final ExpeditionDataImportResponse parse(String json) {
        return AbstractDataImportResponse.parse(json, ExpeditionDataImportResponse.class);
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
        return this.racName;
    }-*/;

}
