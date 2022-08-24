package com.sap.sailing.domain.markpassinghash;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.markpassinghash.impl.MarkPassingHashCalculationFactoryImpl;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface MarkPassingHashCalculationFactory {
    MarkPassingHashCalculationFactory INSTANCE = new MarkPassingHashCalculationFactoryImpl();
    
    MarkPassingHashFingerprint createFingerprint(TrackedRace trackedRace);
    
    MarkPassingHashFingerprint fromJson(JSONObject json);
}
