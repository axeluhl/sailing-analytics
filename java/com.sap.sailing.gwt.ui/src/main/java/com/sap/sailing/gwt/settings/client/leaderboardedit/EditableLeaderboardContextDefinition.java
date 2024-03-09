package com.sap.sailing.gwt.settings.client.leaderboardedit;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.StringSetting;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;

public class EditableLeaderboardContextDefinition extends AbstractGenericSerializableSettings<SecurityChildSettingsContext> {

    private static final long serialVersionUID = -2634678952043877600L;
    
    private transient StringSetting leaderboardName;
    
    public EditableLeaderboardContextDefinition() {
        super(null);
    }
    
    public EditableLeaderboardContextDefinition(String leaderboardName) {
        this();
        this.leaderboardName.setValue(leaderboardName);
    }
    
    @Override
    protected void addChildSettings(SecurityChildSettingsContext context) {
        leaderboardName = new StringSetting("name", this);
    }
    
    public String getLeaderboardName() {
        return leaderboardName.getValue();
    }
}
