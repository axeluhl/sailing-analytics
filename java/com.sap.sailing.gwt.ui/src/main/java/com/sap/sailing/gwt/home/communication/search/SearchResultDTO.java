package com.sap.sailing.gwt.home.communication.search;

import java.net.URL;
import java.util.Date;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.LeaderboardSearchResultBase;
import com.sap.sailing.gwt.dispatch.client.DTO;

public class SearchResultDTO implements DTO {
    
    private String displayName;
    
    private UUID eventId;
    private String eventName;
    private String eventVenueName;
    private Date eventStartDate;
    private Date eventEndDate;
    
    private String leaderboardName;
    private String baseUrl;
    private boolean isOnRemoteServer;
    
    @SuppressWarnings("unused")
    private SearchResultDTO() {
    }
    
    @GwtIncompatible
    public SearchResultDTO(LeaderboardSearchResultBase hit, URL baseUrl, boolean isOnRemoteServer) {
        this.leaderboardName = hit.getLeaderboard().getName();
        this.displayName = hit.getLeaderboard().getDisplayName() != null ? hit.getLeaderboard().getDisplayName() :
            (hit.getRegattaName() != null ? hit.getRegattaName() : leaderboardName);
        EventBase event = hit.getEvent();
        if (event != null) {
            this.eventId = (UUID) event.getId();
            this.eventName = event.getName();
            this.eventVenueName = event.getVenue() != null ? event.getVenue().getName() : null;
            this.eventStartDate = event.getStartDate() != null ? event.getStartDate().asDate() : null;
            this.eventEndDate = event.getEndDate() != null ? event.getEndDate().asDate() : null;
        }
        this.baseUrl = baseUrl.toString();
        this.isOnRemoteServer = isOnRemoteServer;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventVenueName() {
        return eventVenueName;
    }

    public Date getEventStartDate() {
        return eventStartDate;
    }

    public Date getEventEndDate() {
        return eventEndDate;
    }

    public String getLeaderboardName() {
        return leaderboardName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public boolean isOnRemoteServer() {
        return isOnRemoteServer;
    }

}
