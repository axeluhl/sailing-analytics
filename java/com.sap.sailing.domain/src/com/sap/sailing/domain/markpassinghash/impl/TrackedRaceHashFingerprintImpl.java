package com.sap.sailing.domain.markpassinghash.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.markpassinghash.TrackedRaceHashFingerprint;

public class TrackedRaceHashFingerprintImpl implements TrackedRaceHashFingerprint{
    private final int competitor;
    private final int start;
    private final int end;
    private final int waypoints;
    private final int numberOfGPSFixes;
    private final int gpsFixes;

    public TrackedRaceHashFingerprintImpl(Integer competitor, Integer start, Integer end, Integer waypoints,
            Integer numberOfGPSFixes, Integer gpsFixes) {
        this.competitor = competitor;
        this.start = start;
        this.end = end;
        this.waypoints = waypoints;
        this.numberOfGPSFixes = numberOfGPSFixes;
        this.gpsFixes = gpsFixes;
    }

    public JSONObject toJson() {
        return null;
    }

    public int getCompetitor() {
        return competitor;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getWaypoints() {
        return waypoints;
    }

    public int getNumberOfGPSFixes() {
        return numberOfGPSFixes;
    }

    public int getGpsFixes() {
        return gpsFixes;
    }
}
