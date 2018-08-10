package com.sap.sailing.server.impl.preferences.model;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.SailorProfile;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;

public class SailorProfilePreference extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = 8324742146089597693L;
    private transient SailorProfileSetting sailorProfile;

    public SailorProfilePreference(DomainFactory domainFactory) {
        sailorProfile = new SailorProfileSetting("sailorProfile", this, domainFactory);
    }

    public SailorProfilePreference(DomainFactory domainFactory, SailorProfile sailorProfile) {
        this(domainFactory);
        this.sailorProfile.setValue(sailorProfile);
    }

    @Override
    protected void addChildSettings() {
        // We do not create the Setting instances here, because access to the RacingEventService would not be given.
        // Doing this, Java/GWT Serialization isn't working anymore. Because the preferences are only serialized as JSON
        // in the backend an transferred as DTO to the frontend, this isn't a problem. Due to usage of BoatClass and
        // Competitor domain objects, it wouldn't be GWT compatible anyway.
        // The usage of Java Serialization isn't planned by now, either.
    }

    public SailorProfile getSailorProfile() {
        return sailorProfile.getValue();
    }


}
