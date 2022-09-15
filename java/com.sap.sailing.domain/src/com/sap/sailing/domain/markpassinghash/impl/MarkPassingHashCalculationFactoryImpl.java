package com.sap.sailing.domain.markpassinghash.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.markpassinghash.MarkPassingHashFingerprint;
import com.sap.sailing.domain.markpassinghash.MarkPassingHashCalculationFactory;
import com.sap.sailing.domain.tracking.TrackedRace;

public class MarkPassingHashCalculationFactoryImpl
        implements MarkPassingHashCalculationFactory {
    @Override
    public MarkPassingHashFingerprint createFingerprint(TrackedRace trackedRace) {
        return new MarkPassingHashFingerprintImpl(trackedRace);
    }

    @Override
    public MarkPassingHashFingerprint fromJson(JSONObject json) {
        return new MarkPassingHashFingerprintImpl(json);
    }
}
