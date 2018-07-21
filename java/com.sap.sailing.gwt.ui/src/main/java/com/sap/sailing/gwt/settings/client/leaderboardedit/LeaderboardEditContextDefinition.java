package com.sap.sailing.gwt.settings.client.leaderboardedit;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.StringSetting;

public class LeaderboardEditContextDefinition extends AbstractGenericSerializableSettings {

    private static final long serialVersionUID = -2634678952043877600L;
    
    private transient StringSetting leaderboardName;
    
    public LeaderboardEditContextDefinition() {
    }
    
    public LeaderboardEditContextDefinition(String leaderboardName) {
        this.leaderboardName.setValue(leaderboardName);
    }
    
    @Override
    protected void addChildSettings() {
        leaderboardName = new StringSetting("name", this);
    }
    
    public String getLeaderboardName() {
        return leaderboardName.getValue();
    }
}
