package com.sap.sailing.domain.common.preferences;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsFactory;
import com.sap.sse.common.settings.generic.SettingsList;

public class BoatClassNotificationPreferences extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = -3682996540081614053L;
    
    private transient SettingsList<BoatClassNotificationPreference> boatClasses;
    
    public BoatClassNotificationPreferences() {
    }
    
    public BoatClassNotificationPreferences(String name, AbstractGenericSerializableSettings settings) {
        super(name, settings);
    }

    @Override
    protected void addChildSettings() {
        boatClasses = new SettingsList<>("boatClasses", this, new SettingsFactory<BoatClassNotificationPreference>() {
            @Override
            public BoatClassNotificationPreference newInstance() {
                return new BoatClassNotificationPreference();
            }
        });
    }
    
    public Iterable<BoatClassNotificationPreference> getBoatClasses() {
        return boatClasses.getValues();
    }
    
    public void setBoatClasses(Iterable<BoatClassNotificationPreference> boatClasses) {
        this.boatClasses.setValues(boatClasses);
    }
}
