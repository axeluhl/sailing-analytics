package com.sap.sailing.server.interfaces;

import com.sap.sailing.domain.markpassinghash.TrackedRaceHashFingerprint;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface HashFingerprintRegistry {
    void storeFingerprint(TrackedRaceHashFingerprint fingerprint);
    
    TrackedRaceHashFingerprint loadFingerprint(TrackedRace trackedRace);
}