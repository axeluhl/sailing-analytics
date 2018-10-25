package com.sap.sailing.server.impl.preferences.model;

import java.util.UUID;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.StringSetting;
import com.sap.sse.common.settings.generic.UUIDSetting;

public class StoredDataMiningQueryPreference extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = -7100595551754668437L;

    private transient StringSetting name;
    private transient UUIDSetting id;
    private transient StringSetting serializedQuery;

    public StoredDataMiningQueryPreference() {
        name = new StringSetting("name", this);
        id = new UUIDSetting("id", this);
        serializedQuery = new StringSetting("serializedQuery", this);
    }

    public StoredDataMiningQueryPreference(String name, UUID uuid, String serializedQuery) {
        this();
        this.name.setValue(name);
        this.id.setValue(uuid);
        this.serializedQuery.setValue(serializedQuery);
    }

    @Override
    protected void addChildSettings() {
        // We do not create the Setting instances here, because access to the RacingEventService would not be given.
        // Doing this, Java/GWT Serialization isn't working anymore. Because the preferences are only serialized as JSON
        // in the backend an transferred as DTO to the frontend, this isn't a problem. Due to usage of BoatClass and
        // Competitor domain objects, it wouldn't be GWT compatible anyway.
        // The usage of Java Serialization isn't planned by now, either.
    }

    public String getName() {
        return name.getValue();
    }

    public UUID getId() {
        return id.getValue();
    }

    public String getSerializedQuery() {
        return serializedQuery.getValue();
    }
}
