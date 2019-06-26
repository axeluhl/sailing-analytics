package com.sap.sailing.server.impl.preferences.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import com.sap.sse.common.Util;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.BooleanSetting;
import com.sap.sse.common.settings.generic.SettingsList;
import com.sap.sse.common.settings.generic.StringSetting;
import com.sap.sse.common.settings.generic.UUIDSetting;

public class TrackedEventPreference extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = 234711768869820003L;

    private transient UUIDSetting eventId;
    private transient StringSetting regattaId;
    private transient StringSetting baseUrl;
    private transient BooleanSetting isArchived;

    private transient SettingsList<TrackedElementWithDeviceId> trackedElements;

    public TrackedEventPreference() {
    }

    /** copy constructor */
    public TrackedEventPreference(TrackedEventPreference other) {
        this();
        eventId.setValue(other.getEventId());
        regattaId.setValue(other.getRegattaId());
        trackedElements.setValues(other.getTrackedElements());
        baseUrl.setValue(other.getBaseUrl());
        isArchived.setValue(other.getIsArchived());
    }

    /** copy constructor with new archived state */
    public TrackedEventPreference(TrackedEventPreference other, boolean isArchived) {
        this();
        eventId.setValue(other.getEventId());
        regattaId.setValue(other.getRegattaId());
        trackedElements.setValues(other.getTrackedElements());
        baseUrl.setValue(other.getBaseUrl());
        this.isArchived.setValue(isArchived);
    }

    /** copy constructor with additional tracked event */
    public TrackedEventPreference(TrackedEventPreference other, TrackedElementWithDeviceId trackedElement) {
        this();
        eventId.setValue(other.getEventId());
        regattaId.setValue(other.getRegattaId());
        final Collection<TrackedElementWithDeviceId> trackedElements = new HashSet<>();
        Util.addAll(other.getTrackedElements(), trackedElements);
        trackedElements.add(trackedElement);
        this.trackedElements.setValues(trackedElements);
        baseUrl.setValue(other.getBaseUrl());
        this.isArchived.setValue(other.getIsArchived());
    }

    public TrackedEventPreference(UUID eventId, String regattaId, Iterable<TrackedElementWithDeviceId> trackedElements,
            String baseUrl, boolean isArchived) {
        this();
        this.eventId.setValue(eventId);
        this.regattaId.setValue(regattaId);
        this.trackedElements.setValues(trackedElements);
        this.baseUrl.setValue(baseUrl);
        this.isArchived.setValue(isArchived);
    }

    @Override
    protected void addChildSettings() {
        eventId = new UUIDSetting("eventId", this);
        regattaId = new StringSetting("regattaId", this);
        trackedElements = new SettingsList<TrackedElementWithDeviceId>("trackedElements", this,
                () -> new TrackedElementWithDeviceId());
        baseUrl = new StringSetting("baseUrl", this);
        isArchived = new BooleanSetting("isArchived", this);
    }

    public UUID getEventId() {
        return eventId.getValue();
    }

    public String getRegattaId() {
        return regattaId.getValue();
    }

    public Iterable<TrackedElementWithDeviceId> getTrackedElements() {
        return trackedElements.getValues();
    }

    public String getBaseUrl() {
        return baseUrl.getValue();
    }

    public boolean getIsArchived() {
        return isArchived.getValue();
    }
}
