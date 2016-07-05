package com.sap.sailing.domain.common.preferences;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsFactory;
import com.sap.sse.common.settings.generic.SettingsList;

public class CompetitorNotificationPreferences extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = -3682996540081614053L;
    
    private transient SettingsList<CompetitorNotificationPreference> competitors;
    
    public CompetitorNotificationPreferences() {
    }
    
    public CompetitorNotificationPreferences(String name, AbstractGenericSerializableSettings settings) {
        super(name, settings);
    }

    @Override
    protected void addChildSettings() {
        competitors = new SettingsList<>("competitors", this, new SettingsFactory<CompetitorNotificationPreference>() {
            @Override
            public CompetitorNotificationPreference newInstance() {
                return new CompetitorNotificationPreference();
            }
        });
    }
    
    public Iterable<CompetitorNotificationPreference> getCompetitors() {
        return competitors.getValues();
    }
    
    public void setCompetitors(Iterable<CompetitorNotificationPreference> boatClasses) {
        this.competitors.setValues(boatClasses);
    }
}
