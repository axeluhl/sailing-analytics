package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sse.common.settings.AbstractSettings;

/**
 * Settings for a tracked race.
 */
public class TrackedRacesSettings extends AbstractSettings {
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