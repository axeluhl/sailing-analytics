package com.sap.sailing.domain.markpassinghash.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.markpassinghash.MarkPassingRaceFingerprint;
import com.sap.sailing.domain.markpassinghash.MarkPassingRaceFingerprintFactory;
import com.sap.sailing.domain.tracking.TrackedRace;

public class MarkPassingRaceFingerprintFactoryImpl implements MarkPassingRaceFingerprintFactory {
    @Override
    public MarkPassingRaceFingerprint createFingerprint(TrackedRace trackedRace) {
        return new MarkPassingRaceFingerprintImpl(trackedRace);
    }

    @Override
    public MarkPassingRaceFingerprint fromJson(JSONObject json) {
        return new MarkPassingRaceFingerprintImpl(json);
    }
}
