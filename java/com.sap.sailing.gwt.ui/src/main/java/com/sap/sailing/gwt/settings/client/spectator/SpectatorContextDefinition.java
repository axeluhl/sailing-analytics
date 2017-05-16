package com.sap.sailing.gwt.settings.client.spectator;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.StringSetting;

public class SpectatorContextDefinition extends AbstractGenericSerializableSettings {
    
    private static final long serialVersionUID = -3422076606157458337L;
    
    private transient StringSetting leaderboardGroupName;
    
    public SpectatorContextDefinition() {
    }
    
    public SpectatorContextDefinition(String leaderboardGroupName) {
        this.leaderboardGroupName.setValue(leaderboardGroupName);
    }
    
    @Override
    protected void addChildSettings() {
        leaderboardGroupName = new StringSetting("leaderboardGroupName", this);
    }
    
    public String getLeaderboardGroupName() {
        return leaderboardGroupName.getValue();
    }
}
