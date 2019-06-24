package com.sap.sailing.server.impl.preferences.model;

import java.util.UUID;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.BooleanSetting;
import com.sap.sse.common.settings.generic.StringSetting;
import com.sap.sse.common.settings.generic.UUIDSetting;

public class TrackedEventPreference extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = 234711768869820003L;

    private transient UUIDSetting uuid;
    private transient UUIDSetting eventId;
    private transient UUIDSetting regattaId;
    private transient StringSetting trackedElementId;
    private transient StringSetting baseUrl;
    private transient BooleanSetting isArchived;

    public TrackedEventPreference() {
        uuid = new UUIDSetting("uuid", this);
        eventId = new UUIDSetting("eventId", this);
        regattaId = new UUIDSetting("regattaId", this);
        trackedElementId = new StringSetting("trackedElementId", this);
        baseUrl = new StringSetting("baseUrl", this);
        isArchived = new BooleanSetting("isArchived", this);
    }

    /** copy constructor */
    public TrackedEventPreference(TrackedEventPreference other) {
        this();
        uuid.setValue(other.getUuid());
        eventId.setValue(other.getEventId());
        regattaId.setValue(other.getRegattaId());
        trackedElementId.setValue(other.getTrackedElementId());
        baseUrl.setValue(other.getBaseUrl());
        isArchived.setValue(other.getIsArchived());
    }

    /** copy constructor with new archived state */
    public TrackedEventPreference(TrackedEventPreference other, boolean isArchived) {
        this();
        uuid.setValue(other.getUuid());
        eventId.setValue(other.getEventId());
        regattaId.setValue(other.getRegattaId());
        trackedElementId.setValue(other.getTrackedElementId());
        baseUrl.setValue(other.getBaseUrl());
        this.isArchived.setValue(isArchived);
    }

    public TrackedEventPreference(UUID uuid, UUID eventId, UUID regattaId, String trackedElementId, String baseUrl,
            boolean isArchived) {
        this();
        this.uuid.setValue(uuid);
        this.eventId.setValue(eventId);
        this.regattaId.setValue(regattaId);
        this.trackedElementId.setValue(trackedElementId);
        this.baseUrl.setValue(baseUrl);
        this.isArchived.setValue(isArchived);
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

    public UUID getEventId() {
        return eventId.getValue();
    }

    public UUID getRegattaId() {
        return regattaId.getValue();
    }

    public String getTrackedElementId() {
        return trackedElementId.getValue();
    }

    public String getBaseUrl() {
        return baseUrl.getValue();
    }

    public boolean getIsArchived() {
        return isArchived.getValue();
    }
}
