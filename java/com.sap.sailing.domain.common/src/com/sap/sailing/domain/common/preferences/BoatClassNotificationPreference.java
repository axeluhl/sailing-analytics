package com.sap.sailing.domain.common.preferences;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.BooleanSetting;
import com.sap.sse.common.settings.generic.StringSetting;

public class BoatClassNotificationPreference extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = -6510535362114348707L;
    
    private transient StringSetting boatClassName;
    private transient BooleanSetting notifyAboutUpcomingRaces;
    private transient BooleanSetting notifyAboutResults;
    
    public BoatClassNotificationPreference() {
    }
    
    public BoatClassNotificationPreference(String boatClassName, boolean notifyAboutRaces, boolean notifyAboutResults) {
        this.boatClassName.setValue(boatClassName);
        this.notifyAboutUpcomingRaces.setValue(notifyAboutRaces);
        this.notifyAboutResults.setValue(notifyAboutResults);
    }
    
    @Override
    protected void addChildSettings() {
        boatClassName = new StringSetting("boatClassName", this);
        notifyAboutUpcomingRaces = new BooleanSetting("notifyAboutUpcomingRaces", this, false);
        notifyAboutResults = new BooleanSetting("notifyAboutResults", this, false);
    }
    
    public String getBoatClassName() {
        return boatClassName.getValue();
    }
    
    public boolean isNotifyAboutUpcomingRaces() {
        return Boolean.TRUE.equals(notifyAboutUpcomingRaces.getValue());
    }
    
    public boolean isNotifyAboutResults() {
        return Boolean.TRUE.equals(notifyAboutResults.getValue());
    }
}
