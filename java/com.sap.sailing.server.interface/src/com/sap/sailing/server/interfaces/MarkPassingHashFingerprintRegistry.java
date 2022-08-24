package com.sap.sailing.server.interfaces;

import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.markpassinghash.MarkPassingHashFingerprint;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface MarkPassingHashFingerprintRegistry {
    void storeFingerprint(RaceIdentifier raceIdentifier, MarkPassingHashFingerprint fingerprint);
    
    MarkPassingHashFingerprint loadFingerprint(TrackedRace trackedRace);
}