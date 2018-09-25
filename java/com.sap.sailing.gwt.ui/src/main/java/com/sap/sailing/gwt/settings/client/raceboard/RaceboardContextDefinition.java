package com.sap.sailing.gwt.settings.client.raceboard;

import java.util.UUID;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.StringSetting;
import com.sap.sse.common.settings.generic.UUIDSetting;

public class RaceboardContextDefinition extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = 2632481216072003426L;
    
    private StringSetting regattaName;
    private StringSetting raceName;
    private StringSetting leaderboardName;
    private StringSetting leaderboardGroupName;
    private UUIDSetting eventId;
    private StringSetting mode;
    private StringSetting selectedCompetitor;

    public RaceboardContextDefinition() {
    }

    public RaceboardContextDefinition(String regattaName, String raceName, String leaderboardName,
            String leaderboardGroupName, UUID eventId, String mode) {
        this(regattaName, raceName, leaderboardName, leaderboardGroupName, eventId, mode, null);
    }

    public RaceboardContextDefinition(String regattaName, String raceName, String leaderboardName,
            String leaderboardGroupName, UUID eventId, String mode, String selectedCompetitorId) {
        this.regattaName.setValue(regattaName);
        this.raceName.setValue(raceName);
        this.leaderboardName.setValue(leaderboardName);
        this.leaderboardGroupName.setValue(leaderboardGroupName);
        this.eventId.setValue(eventId);
        this.mode.setValue(mode);
        this.selectedCompetitor.setValue(selectedCompetitorId);
    }

    @Override
    protected void addChildSettings() {
        regattaName = new StringSetting("regattaName", this);
        raceName = new StringSetting("raceName", this);
        leaderboardName = new StringSetting("leaderboardName", this);
        leaderboardGroupName = new StringSetting("leaderboardGroupName", this);
        eventId = new UUIDSetting("eventId", this);
        mode = new StringSetting("mode", this);
        selectedCompetitor = new StringSetting("c", this);
    }

    public String getRegattaName() {
        return regattaName.getValue();
    }

    public String getRaceName() {
        return raceName.getValue();
    }

    public String getLeaderboardName() {
        return leaderboardName.getValue();
    }

    public String getLeaderboardGroupName() {
        return leaderboardGroupName.getValue();
    }

    public UUID getEventId() {
        return eventId.getValue();
    }

    public String getMode() {
        return mode.getValue();
    }

    public String getSelectedCompetitor() {
        return selectedCompetitor.getValue();
    }
}
