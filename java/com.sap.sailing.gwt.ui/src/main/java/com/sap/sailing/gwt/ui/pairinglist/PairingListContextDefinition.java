package com.sap.sailing.gwt.ui.pairinglist;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.IntegerSetting;
import com.sap.sse.common.settings.generic.StringSetting;

public class PairingListContextDefinition extends AbstractGenericSerializableSettings {
    
    private static final long serialVersionUID = 6206953942932913058L;
    
    private transient StringSetting leaderboardName;
    private transient IntegerSetting flightMultiplier;
    
    public PairingListContextDefinition() { }
    
    public PairingListContextDefinition(String leaderboardName, int flightMultiplier) {
        this.leaderboardName.setValue(leaderboardName);
        this.flightMultiplier.setValue(flightMultiplier);
    }
    
    @Override
    protected void addChildSettings() {
        this.leaderboardName = new StringSetting("leaderboardName", this);
        this.flightMultiplier = new IntegerSetting("flightMultiplier", this);
    }
    
    public String getLeaderboardName() {
        return this.leaderboardName.getValue();
    }
    
    public int getFlightMultiplier() {
        return this.flightMultiplier.getValue();
    }
}
