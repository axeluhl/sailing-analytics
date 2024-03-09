package com.sap.sailing.gwt.ui.pairinglist;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.StringSetting;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;

public class PairingListContextDefinition extends AbstractGenericSerializableSettings<SecurityChildSettingsContext> {
    
    private static final long serialVersionUID = 6206953942932913058L;
    
    private transient StringSetting leaderboardName;

    public PairingListContextDefinition() {
        super(null);
    }
    
    public PairingListContextDefinition(String leaderboardName) {
        this();
        this.leaderboardName.setValue(leaderboardName);
    }
    
    @Override
    protected void addChildSettings(SecurityChildSettingsContext context) {
        this.leaderboardName = new StringSetting("leaderboardName", this);
    }
    
    public String getLeaderboardName() {
        return this.leaderboardName.getValue();
    }
}
