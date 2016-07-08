package com.sap.sailing.domain.common.preferences;

import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.BooleanSetting;

public class BoatClassNotificationPreference extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = -6510535362114348707L;
    
    private transient BoatClassSetting boatClass;
    private transient BooleanSetting notifyAboutUpcomingRaces;
    private transient BooleanSetting notifyAboutResults;
    
    public BoatClassNotificationPreference() {
    }
    
    public BoatClassNotificationPreference(BoatClassMasterdata boatClass, boolean notifyAboutRaces, boolean notifyAboutResults) {
        this.boatClass.setValue(boatClass);
        this.notifyAboutUpcomingRaces.setValue(notifyAboutRaces);
        this.notifyAboutResults.setValue(notifyAboutResults);
    }
    
    @Override
    protected void addChildSettings() {
        boatClass = new BoatClassSetting("boatClass", this);
        notifyAboutUpcomingRaces = new BooleanSetting("notifyAboutUpcomingRaces", this, false);
        notifyAboutResults = new BooleanSetting("notifyAboutResults", this, false);
    }
    
    public BoatClassMasterdata getBoatClass() {
        return boatClass.getValue();
    }
    
    public boolean isNotifyAboutUpcomingRaces() {
        return Boolean.TRUE.equals(notifyAboutUpcomingRaces.getValue());
    }
    
    public boolean isNotifyAboutResults() {
        return Boolean.TRUE.equals(notifyAboutResults.getValue());
    }
}
