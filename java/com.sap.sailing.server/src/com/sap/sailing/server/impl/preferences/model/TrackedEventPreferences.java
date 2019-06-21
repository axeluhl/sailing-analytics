package com.sap.sailing.server.impl.preferences.model;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsList;

public class TrackedEventPreferences extends AbstractGenericSerializableSettings {

    private static final long serialVersionUID = -290209497308646446L;

    // TODO: setting preference title
    public static final String PREF_NAME = SailingPreferences.TRACKED_EVENTS_PREFERENCES;

    private transient SettingsList<TrackedEventPreference> trackedEvents;

    public TrackedEventPreferences() {
        trackedEvents = new SettingsList<>("trackedEvents", this, () -> new TrackedEventPreference());
    }

    @Override
    protected void addChildSettings() {
        // We do not create the Setting instances here, because access to the RacingEventService would not be given.
        // Doing this, Java/GWT Serialization isn't working anymore. Because the preferences are only serialized as JSON
        // in the backend an transferred as DTO to the frontend, this isn't a problem. Due to usage of BoatClass and
        // Competitor domain objects, it wouldn't be GWT compatible anyway.
        // The usage of Java Serialization isn't planned by now, either.
    }

    public Iterable<TrackedEventPreference> getTrackedEvents() {
        return trackedEvents.getValues();
    }

    public void setTrackedEvents(Iterable<TrackedEventPreference> trackedEvents) {
        this.trackedEvents.setValues(trackedEvents);
    }
}
