package com.sap.sailing.server.impl.preferences.model;

import java.util.UUID;

import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.SailorProfile;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.StringSetting;
import com.sap.sse.common.settings.generic.UUIDSetting;

public class SailorProfilePreference extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = 8324742146089597693L;

    private transient UUIDSetting uuid;
    private transient StringSetting name;

    private transient CompetitorSetSetting competitors;

    public SailorProfilePreference(CompetitorAndBoatStore store) {
        uuid = new UUIDSetting("uuid", this);
        name = new StringSetting("name", this);
        competitors = new CompetitorSetSetting("competitors", this, store);
    }

    public SailorProfilePreference(CompetitorAndBoatStore store, SailorProfile sailorProfile) {
        this(store);
        uuid.setValue(sailorProfile.getUuid());
        name.setValue(sailorProfile.getName());
        competitors.setValues(sailorProfile.getCompetitors());
    }

    @Override
    protected void addChildSettings() {
        // We do not create the Setting instances here, because access to the RacingEventService would not be given.
        // Doing this, Java/GWT Serialization isn't working anymore. Because the preferences are only serialized as JSON
        // in the backend an transferred as DTO to the frontend, this isn't a problem. Due to usage of BoatClass and
        // Competitor domain objects, it wouldn't be GWT compatible anyway.
        // The usage of Java Serialization isn't planned by now, either.
    }

    public UUID getUuid() {
        return uuid.getValue();
    }
}
