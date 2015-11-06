package com.sap.sailing.gwt.home.communication.search;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.LeaderboardSearchResultBase;
import com.sap.sailing.gwt.dispatch.client.DTO;

public class SearchResultDTO implements DTO {
    
    private String displayName;
    private String leaderboardName;
    private String baseUrl;
    private boolean isOnRemoteServer;
    private ArrayList<EventInfoDTO> events = new ArrayList<>();
    
    @SuppressWarnings("unused")
    private SearchResultDTO() {
    }
    
    @GwtIncompatible
    public SearchResultDTO(LeaderboardSearchResultBase hit, URL baseUrl, boolean isOnRemoteServer) {
        this.leaderboardName = hit.getLeaderboard().getName();
        this.displayName = hit.getLeaderboard().getDisplayName() != null ? hit.getLeaderboard().getDisplayName() :
            (hit.getRegattaName() != null ? hit.getRegattaName() : leaderboardName);
        for (EventBase event : hit.getEvents()) {
            events.add(new EventInfoDTO(event));
        }
        this.baseUrl = baseUrl.toString();
        this.isOnRemoteServer = isOnRemoteServer;
    }

    public String getDisplayName() {
        return displayName;
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
    
    public ArrayList<EventInfoDTO> getEvents() {
        return events;
    }
    
    public class EventInfoDTO implements DTO {
        private UUID id;
        private String name;
        private String venueName;
        private Date startDate;
        private Date endDate;
        
        @GwtIncompatible
        public EventInfoDTO(EventBase event) {
            this.id = (UUID) event.getId();
            this.name = event.getName();
            this.venueName = event.getVenue() != null ? event.getVenue().getName() : null;
            this.startDate = event.getStartDate() != null ? event.getStartDate().asDate() : null;
            this.endDate = event.getEndDate() != null ? event.getEndDate().asDate() : null;
        }

        public UUID getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }

        public String getVenueName() {
            return venueName;
        }

        public Date getStartDate() {
            return startDate;
        }

        public Date getEndDate() {
            return endDate;
        }
    }
}
