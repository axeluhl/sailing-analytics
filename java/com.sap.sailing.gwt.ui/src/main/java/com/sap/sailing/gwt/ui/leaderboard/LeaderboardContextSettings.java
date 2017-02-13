package com.sap.sailing.gwt.ui.leaderboard;

import java.util.UUID;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.BooleanSetting;
import com.sap.sse.common.settings.generic.StringSetting;
import com.sap.sse.common.settings.generic.UUIDSetting;

public class LeaderboardContextSettings extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = 6699950643747065960L;
    
    private transient BooleanSetting embedded;
    private transient UUIDSetting eventId;
    private transient StringSetting leaderboardName;
    private transient StringSetting leaderboardGroupName;
    private transient StringSetting displayName;
    private transient StringSetting zoomTo;
    private transient StringSetting raceName;
    private transient StringSetting regattaName;

    public LeaderboardContextSettings() {
    }

    public LeaderboardContextSettings(String leaderboardName, String displayName) {
        this.leaderboardName.setValue(leaderboardName);
        this.displayName.setValue(displayName);
    }

    @Override
    protected void addChildSettings() {
        embedded = new BooleanSetting(LeaderboardUrlSettings.PARAM_EMBEDDED, this, false);
        eventId = new UUIDSetting(LeaderboardUrlSettings.PARAM_EVENT_ID, this);
        leaderboardName = new StringSetting("name", this);
        leaderboardGroupName = new StringSetting(LeaderboardUrlSettings.PARAM_LEADERBOARD_GROUP_NAME, this);
        displayName = new StringSetting("displayName", this);
        zoomTo = new StringSetting(LeaderboardUrlSettings.PARAM_ZOOM_TO, this);
        raceName = new StringSetting(LeaderboardUrlSettings.PARAM_RACE_NAME, this);
        regattaName = new StringSetting(LeaderboardUrlSettings.PARAM_REGATTA_NAME, this);
    }
    public boolean getEmbedded() {
        return embedded.getValue();
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
    public String getZoomTo() {
        return zoomTo.getValue();
    }
    public String getRaceName() {
        return raceName.getValue();
    }
    public String getRegattaName() {
        return regattaName.getValue();
    }
    
    
}
