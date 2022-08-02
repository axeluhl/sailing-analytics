package com.sap.sailing.domain.markpassinghash.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.markpassinghash.TrackedRaceHashFingerprint;
import com.sap.sailing.domain.markpassinghash.TrackedRaceHashForMaskPassingCalculationFactory;
import com.sap.sailing.domain.tracking.TrackedRace;

public class TrackedRaceHashForMarkPassingCalculationFactoryImpl
        implements TrackedRaceHashForMaskPassingCalculationFactory {
    @Override
    public TrackedRaceHashFingerprint createFingerprint(TrackedRace trackedRace) {
        return new TrackedRaceHashFingerprintImpl(trackedRace);
    }

    @Override
    public TrackedRaceHashFingerprint fromJson(JSONObject json) {
        return new TrackedRaceHashFingerprintImpl(json);
    }
}
