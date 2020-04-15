package com.sap.sailing.android.tracking.app.valueobjects;

import com.sap.sailing.domain.common.DeviceIdentifier;

public abstract class UrlData {
    public String uriStr;
    public String server;
    public int port;
    public String hostWithPort;
    public String checkinURLStr;
    public String secret;
    public String eventId;
    public String leaderboardName;
    public DeviceIdentifier deviceUuid;
    public String eventUrl;
    public String leaderboardUrl;
    public String eventName;
    public String eventStartDateStr;
    public String eventEndDateStr;
    public String eventFirstImageUrl;
    public String checkinURL;
    public String uriString;

    public UrlData(String server, int port) {
        this.server = server;
        this.port = port;
        hostWithPort = server + (port == -1 ? "" : (":" + port));
    }
}
