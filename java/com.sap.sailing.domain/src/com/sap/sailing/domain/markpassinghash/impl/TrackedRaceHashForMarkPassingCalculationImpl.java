package com.sap.sailing.domain.markpassinghash.impl;

import com.sap.sailing.domain.tracking.impl.TrackedRaceImpl;

public class TrackedRaceHashForMarkPassingCalculationImpl {
    private final TrackedRaceHashFingerprintImpl hashFingerprint;

    public TrackedRaceHashForMarkPassingCalculationImpl(TrackedRaceHashFingerprintImpl hashFingerprint) {
        this.hashFingerprint = hashFingerprint;
    }

    public boolean equals(TrackedRaceImpl trackedRace) {
        TrackedRaceHashForMarkPassingCalculationFactoryImpl factory = new TrackedRaceHashForMarkPassingCalculationFactoryImpl();
        TrackedRaceHashFingerprintImpl trackedRaceHashFingerprint = factory.create(trackedRace);

        if (hashFingerprint.getWaypoints() != trackedRaceHashFingerprint.getWaypoints())
            return false;

        if (hashFingerprint.getCompetitor() != trackedRaceHashFingerprint.getCompetitor())
            return false;

        if (hashFingerprint.getStart() != trackedRaceHashFingerprint.getStart())
            return false;

        if (hashFingerprint.getEnd() != trackedRaceHashFingerprint.getEnd())
            return false;

        if (hashFingerprint.getNumberOfGPSFixes() != trackedRaceHashFingerprint.getNumberOfGPSFixes())
            return false;

        if (hashFingerprint.getGpsFixes() != trackedRaceHashFingerprint.getGpsFixes())
            return false;

        return true;
    }
}