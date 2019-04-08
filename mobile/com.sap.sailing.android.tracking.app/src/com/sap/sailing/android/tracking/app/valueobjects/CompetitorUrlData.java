package com.sap.sailing.android.tracking.app.valueobjects;

public class CompetitorUrlData extends UrlData {

    public String competitorId;
    public String competitorUrl;
    public String competitorName;
    public String competitorSailId;
    public String competitorNationality;
    public String competitorCountryCode;

    public CompetitorUrlData(String server, int port) {
        super(server, port);
    }
}
