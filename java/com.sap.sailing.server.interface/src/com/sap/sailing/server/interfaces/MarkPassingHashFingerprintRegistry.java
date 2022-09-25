package com.sap.sailing.server.interfaces;

import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.markpassinghash.MarkPassingHashFingerprint;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * A Registry to reveal the {link MarkPassingHashFingerprint}s to the different levels of the domain model.
 *
 * @author Fabian Kallenbach (i550803)
 */
public interface MarkPassingHashFingerprintRegistry {
    void storeFingerprint(RaceIdentifier raceIdentifier, MarkPassingHashFingerprint fingerprint);

    MarkPassingHashFingerprint loadFingerprint(TrackedRace trackedRace);
}