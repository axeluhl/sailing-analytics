package com.sap.sailing.domain.common.preferences;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.BooleanSetting;
import com.sap.sse.common.settings.generic.StringSetting;

public class CompetitorNotificationPreference extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = -6510535362114348707L;
    
    private transient StringSetting competitorId;
    private transient BooleanSetting notifyAboutResults;
    
    public CompetitorNotificationPreference() {
    }
    
    public CompetitorNotificationPreference(String competitorId, boolean notifyAboutResults) {
        this.competitorId.setValue(competitorId);
        this.notifyAboutResults.setValue(notifyAboutResults);
    }
    
    @Override
    protected void addChildSettings() {
        competitorId = new StringSetting("competitorId", this);
        notifyAboutResults = new BooleanSetting("notifyAboutResults", this, false);
    }
    
    public String getCompetitorId() {
        return competitorId.getValue();
    }
    
    public boolean isNotifyAboutResults() {
        return Boolean.TRUE.equals(notifyAboutResults.getValue());
    }
}
