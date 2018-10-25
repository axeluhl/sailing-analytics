package com.sap.sailing.server.impl.preferences.model;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsList;

public class StoredDataMiningQueryPreferences extends AbstractGenericSerializableSettings {

    private static final long serialVersionUID = -8088467604778160161L;
    public static final String PREF_NAME = SailingPreferences.STORED_DATAMINING_QUERY_PREFERENCES;

    private transient SettingsList<StoredDataMiningQueryPreference> storedQueries;

    public StoredDataMiningQueryPreferences() {
        storedQueries = new SettingsList<>("storedQueries", this, () -> new StoredDataMiningQueryPreference());
    }

    @Override
    protected void addChildSettings() {
        // We do not create the Setting instances here, because access to the RacingEventService would not be given.
        // Doing this, Java/GWT Serialization isn't working anymore. Because the preferences are only serialized as JSON
        // in the backend an transferred as DTO to the frontend, this isn't a problem. Due to usage of BoatClass and
        // Competitor domain objects, it wouldn't be GWT compatible anyway.
        // The usage of Java Serialization isn't planned by now, either.
    }

    public Iterable<StoredDataMiningQueryPreference> getStoredQueries() {
        return storedQueries.getValues();
    }

    public void setStoredQueries(Iterable<StoredDataMiningQueryPreference> storedQueries) {
        this.storedQueries.setValues(storedQueries);
    }
}
