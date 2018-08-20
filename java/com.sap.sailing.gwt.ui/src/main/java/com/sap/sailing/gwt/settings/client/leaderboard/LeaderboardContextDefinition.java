package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.UUID;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.StringSetting;
import com.sap.sse.common.settings.generic.UUIDSetting;

public class LeaderboardContextDefinition extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = 6699950643747065960L;
    
    private transient UUIDSetting eventId;
    private transient StringSetting leaderboardName;
    private transient StringSetting leaderboardGroupName;
    private transient StringSetting displayName;

    public LeaderboardContextDefinition() {
    }
    
    public LeaderboardContextDefinition(String leaderboardName, String displayName, String leaderboardGroupName) {
        this(leaderboardName, displayName);
        this.leaderboardGroupName.setValue(leaderboardGroupName);
    }

    public LeaderboardContextDefinition(String leaderboardName, String displayName) {
        this.leaderboardName.setValue(leaderboardName);
        this.displayName.setValue(displayName);
    }

    @Override
    protected void addChildSettings() {
        eventId = new UUIDSetting("eventId", this);
        leaderboardName = new StringSetting("name", this);
        leaderboardGroupName = new StringSetting("leaderboardGroupName", this);
        displayName = new StringSetting("displayName", this);
    }
    public UUID getEventId() {
        return eventId.getValue();
    }
    public String getLeaderboardName() {
        return leaderboardName.getValue();
    }
    public String getLeaderboardGroupName() {
        return leaderboardGroupName.getValue();
    }
    public String getDisplayName() {
        return displayName.getValue();
    }
}
