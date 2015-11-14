package com.sap.sailing.gwt.home.communication.event.news;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.news.impl.LeaderboardUpdateNewsItem;

public class LeaderboardNewsEntryDTO extends NewsEntryDTO {
    
    public enum Type {
        RESULTS_UPDATE
    }

    private String leaderboardName;
    private String boatClassName;
    private Type type;

    @SuppressWarnings("unused")
    private LeaderboardNewsEntryDTO() {
    }

    @GwtIncompatible
    public LeaderboardNewsEntryDTO(LeaderboardUpdateNewsItem item) {
        super(item.getTitle(), item.getCreatedAtDate(), null);
        this.boatClassName = item.getBoatClass();
        this.type = Type.RESULTS_UPDATE;
        this.leaderboardName = item.getLeaderboardName();
    }

    @Override
    public String getBoatClass() {
        return boatClassName;
    }
    
    @Override
    public String getMessage() {
        switch (type) {
        case RESULTS_UPDATE:
            return StringMessages.INSTANCE.resultsUpdate();
        }
        return "";
    }

    public String getLeaderboardName() {
        return leaderboardName;
    }
}
