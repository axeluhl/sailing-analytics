package com.sap.sailing.domain.markpassinghash;

import com.sap.sailing.domain.markpassinghash.impl.TrackedRaceHashFingerprintImpl;
import com.sap.sailing.domain.markpassinghash.impl.TrackedRaceHashForMarkPassingCalculationImpl;
import com.sap.sailing.domain.tracking.impl.TrackedRaceImpl;

public interface TrackedRaceHashForMaskPassingCalculationFactory {

    TrackedRaceHashFingerprintImpl create(TrackedRaceImpl trackedRace);
}
