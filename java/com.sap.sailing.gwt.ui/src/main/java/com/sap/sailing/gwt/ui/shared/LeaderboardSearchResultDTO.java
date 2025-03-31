package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.base.LeaderboardSearchResultBase;
import com.sap.sailing.domain.base.RemoteSailingServerReference;

/**
 * The DTO version of a {@link LeaderboardSearchResultBase} object for transmission to clients. In addition to the
 * fields of a {@link LeaderboardSearchResultBase}, this object also tells the base URL of the server from which this
 * result was obtained. This may be a server different from the one to which the query was sent, as such a server may
 * maintain a set of {@link RemoteSailingServerReference}s pointing to other servers from where search results can be
 * obtained as well. The client needs to use this base URL to navigate to a user interface / page that can display those
 * search results.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class LeaderboardSearchResultDTO implements IsSerializable {
    private String baseURL;
    private boolean isOnRemoteServer;
    private String leaderboardName;
    private String leaderboardDisplayName;
    private String regattaName;
    private String boatClassName;
    private Iterable<EventBaseDTO> events;
    private Iterable<LeaderboardGroupBaseDTO> leaderboardGroups;
    
    LeaderboardSearchResultDTO() {} // for GWT RPC serialization only
    
    public LeaderboardSearchResultDTO(String baseURL, boolean isOnRemoteServer, String leaderboardName, String leaderboardDisplayName,
            String regattaName, String boatClassName, Iterable<EventBaseDTO> events,
            Iterable<LeaderboardGroupBaseDTO> leaderboardGroups) {
        super();
        this.baseURL = baseURL;
        this.isOnRemoteServer = isOnRemoteServer;
        this.leaderboardName = leaderboardName;
        this.leaderboardDisplayName = leaderboardDisplayName;
        this.regattaName = regattaName;
        this.boatClassName = boatClassName;
        this.events = events;
        this.leaderboardGroups = leaderboardGroups;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public String getLeaderboardName() {
        return leaderboardName;
    }

    public String getLeaderboardDisplayName() {
        return leaderboardDisplayName;
    }

    public String getRegattaName() {
        return regattaName;
    }

    public String getBoatClassName() {
        return boatClassName;
    }

    public Iterable<EventBaseDTO> getEvents() {
        return events;
    }

    public Iterable<LeaderboardGroupBaseDTO> getLeaderboardGroups() {
        return leaderboardGroups;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Search result from ");
        sb.append(getBaseURL());
        sb.append(": Leaderboard ");
        sb.append(getLeaderboardName());
        if (getLeaderboardDisplayName() != null) {
            sb.append(" (display name: ");
            sb.append(getLeaderboardDisplayName());
            sb.append(")");
        }
        if (getEvents() != null) {
            sb.append(", Events ");
            sb.append(getEvents());
        }
        sb.append(", ");
        sb.append("Boat class: ");
        sb.append(getBoatClassName());
        sb.append("; ");
        sb.append("Regatta Name: ");
        sb.append(getRegattaName());
        return sb.toString();
    }

    public boolean isOnRemoteServer() {
        return isOnRemoteServer;
    }
}
