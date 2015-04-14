package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.domain.common.settings.Settings;

/**
 * Settings for a tracked race.
 */
public class TrackedRacesSettings implements Settings {
    private long delayToLiveInSeconds;

    public TrackedRacesSettings() {
    }

    public long getDelayToLiveInSeconds() {
        return delayToLiveInSeconds;
    }

    public void setDelayToLiveInSeconds(long delayToLiveInSeconds) {
        this.delayToLiveInSeconds = delayToLiveInSeconds;
    }
}