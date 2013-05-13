package com.sap.sailing.gwt.ui.adminconsole;

/**
 * Settings for a tracked race.
 */
public class TrackedRacesSettings {
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