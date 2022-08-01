package com.sap.sailing.domain.markpassinghash;

import com.sap.sailing.domain.markpassinghash.impl.TrackedRaceHashForMarkPassingCalculationImpl;
import com.sap.sailing.domain.tracking.impl.TrackedRaceImpl;

public interface TrackedRaceHashForMaskPassingCalculationFactory {

    TrackedRaceHashForMarkPassingCalculationImpl create(TrackedRaceImpl trackedRace);
}
