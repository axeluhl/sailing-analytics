package com.sap.sailing.server.interfaces;

import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.markpassinghash.TrackedRaceHashFingerprint;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface HashFingerprintRegistry {
    void storeFingerprint(RaceIdentifier raceIdentifier, TrackedRaceHashFingerprint fingerprint);
    
    TrackedRaceHashFingerprint loadFingerprint(TrackedRace trackedRace);
}