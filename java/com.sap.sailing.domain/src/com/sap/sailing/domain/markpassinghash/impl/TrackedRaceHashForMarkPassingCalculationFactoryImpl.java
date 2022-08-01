package com.sap.sailing.domain.markpassinghash.impl;

import com.sap.sailing.domain.markpassinghash.TrackedRaceHashForMaskPassingCalculationFactory;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.TrackedRaceImpl;

public class TrackedRaceHashForMarkPassingCalculationFactoryImpl implements TrackedRaceHashForMaskPassingCalculationFactory{
    private final TrackedRace trackedRace;
    
    public TrackedRaceHashForMarkPassingCalculationFactoryImpl(TrackedRaceImpl trackedRace) {
        this.trackedRace = trackedRace;
    }

    @Override
    public TrackedRaceHashForMarkPassingCalculationImpl create(TrackedRaceImpl trackedRace) {
        
        TrackedRaceHashForMarkPassingCalculationImpl hashValues = new TrackedRaceHashForMarkPassingCalculationImpl(trackedRace.getHashValuesForMarkPassingCalculation());
        return hashValues;
    }
}
