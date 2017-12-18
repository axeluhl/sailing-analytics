package com.sap.sailing.gwt.ui.adminconsole.resulthandling;

import java.util.UUID;
import java.util.logging.Logger;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONParser;

public class ExpeditionDataImportResponse extends JavaScriptObject {

    private static final Logger logger = Logger.getLogger(ExpeditionDataImportResponse.class.getName());

    public static final ExpeditionDataImportResponse parse(String json) {
        try {
            return (ExpeditionDataImportResponse) JSONParser.parseStrict(json).isObject().getJavaScriptObject();
        } catch (Exception e) {
            logger.severe("Failed to parse ExpeditionDataImportResponse object!");
            return null;
        }
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
