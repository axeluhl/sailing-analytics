package com.sap.sailing.gwt.ui.shared.dispatch.news;

import java.util.Date;

public class LeaderboardNewsEntryDTO extends NewsEntryDTO {
    
    public enum Type {
        NEW_RESULTS
    }

    private String leaderboardName;
    private String leaderboardDisplayName;
    private String boatClassName;
    private String externalURL;
    private Type type;

    @SuppressWarnings("unused")
    private LeaderboardNewsEntryDTO() {
    }

    public LeaderboardNewsEntryDTO(String leaderboardName, String leaderboardDisplayName, String boatClassName, Date timestamp, Type type) {
        super(timestamp);
        this.boatClassName = boatClassName;
        this.type = type;
        this.setLeaderboardName(leaderboardName);
        this.leaderboardDisplayName = leaderboardDisplayName;
    }

    @Override
    public String getTitle() {
        return leaderboardDisplayName;
    }
    
    @Override
    public String getBoatClass() {
        return boatClassName;
    }
    
    @Override
    public String getMessage() {
        switch (type) {
        case NEW_RESULTS:
            return "New results are available";
        }
        return "";
    }

    public String getExternalURL() {
        return externalURL;
    }

    public void setExternalURL(String externalURL) {
        this.externalURL = externalURL;
    }

    public String getLeaderboardName() {
        return leaderboardName;
    }

    public void setLeaderboardName(String leaderboardName) {
        this.leaderboardName = leaderboardName;
    }
}
