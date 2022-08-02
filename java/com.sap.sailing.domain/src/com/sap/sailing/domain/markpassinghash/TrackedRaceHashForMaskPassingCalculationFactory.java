package com.sap.sailing.domain.markpassinghash;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.markpassinghash.impl.TrackedRaceHashForMarkPassingCalculationFactoryImpl;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface TrackedRaceHashForMaskPassingCalculationFactory {
    TrackedRaceHashForMaskPassingCalculationFactory INSTANCE = new TrackedRaceHashForMarkPassingCalculationFactoryImpl();
    
    TrackedRaceHashFingerprint createFingerprint(TrackedRace trackedRace);
    
    TrackedRaceHashFingerprint fromJson(JSONObject json);
}
