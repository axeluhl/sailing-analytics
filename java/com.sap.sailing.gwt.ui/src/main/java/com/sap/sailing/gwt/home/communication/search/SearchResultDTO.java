package com.sap.sailing.gwt.home.communication.search;

import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.LeaderboardSearchResultBase;
import com.sap.sailing.gwt.dispatch.client.DTO;

public class SearchResultDTO implements DTO {
    
    private String displayName;
    private String leaderboardName;
    private String baseUrl;
    private boolean isOnRemoteServer;
    private TreeSet<SearchResultEventInfoDTO> events = new TreeSet<>();
    
    @SuppressWarnings("unused")
    private SearchResultDTO() {
    }
    
    @GwtIncompatible
    public SearchResultDTO(LeaderboardSearchResultBase hit, URL baseUrl, boolean isOnRemoteServer) {
        this.leaderboardName = hit.getLeaderboard().getName();
        this.displayName = hit.getLeaderboard().getDisplayName() != null ? hit.getLeaderboard().getDisplayName() :
            (hit.getRegattaName() != null ? hit.getRegattaName() : leaderboardName);
        this.baseUrl = baseUrl.toString();
        this.isOnRemoteServer = isOnRemoteServer;
        for (EventBase event : hit.getEvents()) {
            events.add(new SearchResultEventInfoDTO(event));
        }
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
    
    public Set<SearchResultEventInfoDTO> getEvents() {
        return events;
    }
    
}
