package com.sap.sailing.gwt.ui.shared.dispatch.news;

import java.util.Date;

public abstract class AbstractLeaderboardNewsEntryDTO extends NewsEntryDTO {

    private String leaderboardName;
    private String leaderboardDisplayName;
    private String boatClassName;
    private String externalURL;

    @SuppressWarnings("unused")
    private AbstractLeaderboardNewsEntryDTO() {
    }

    public AbstractLeaderboardNewsEntryDTO(String leaderboardName, String leaderboardDisplayName, String boatClassName, Date timestamp) {
        super(timestamp);
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
