package com.sap.sailing.gwt.ui.pairinglist;

import java.util.Arrays;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.IntegerSetting;
import com.sap.sse.common.settings.generic.StringSetting;

public class PairingListContextDefinition extends AbstractGenericSerializableSettings {
    
    private static final long serialVersionUID = 6206953942932913058L;
    
    private transient StringSetting leaderboardName;
    private transient IntegerSetting flightMultiplier;
    private transient StringSetting selectedFlightNames;

    public PairingListContextDefinition() { }
    
    public PairingListContextDefinition(String leaderboardName, int flightMultiplier, String selectedFlightNames) {
        this.leaderboardName.setValue(leaderboardName);
        this.flightMultiplier.setValue(flightMultiplier);
        this.selectedFlightNames.setValue(selectedFlightNames);
    }
    
    @Override
    protected void addChildSettings() {
        this.leaderboardName = new StringSetting("leaderboardName", this);
        this.flightMultiplier = new IntegerSetting("flightMultiplier", this);
        this.selectedFlightNames = new StringSetting("selectedFlights", this);
    }
    
    public String getLeaderboardName() {
        return this.leaderboardName.getValue();
    }
    
    public int getFlightMultiplier() {
        return this.flightMultiplier.getValue();
    }
    
    public Iterable<String> getSelectedFlightNames() {
        return Arrays.asList(selectedFlightNames.getValue().split(","));
    }
}
