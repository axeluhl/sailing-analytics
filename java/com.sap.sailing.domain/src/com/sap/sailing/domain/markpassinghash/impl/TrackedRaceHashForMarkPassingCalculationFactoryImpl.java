package com.sap.sailing.domain.markpassinghash.impl;

import com.sap.sailing.domain.markpassinghash.TrackedRaceHashForMaskPassingCalculationFactory;
import com.sap.sailing.domain.tracking.impl.TrackedRaceImpl;

public class TrackedRaceHashForMarkPassingCalculationFactoryImpl implements TrackedRaceHashForMaskPassingCalculationFactory{
    
    public TrackedRaceHashForMarkPassingCalculationFactoryImpl() {
    }

    @Override
    public TrackedRaceHashForMarkPassingCalculationImpl create(TrackedRaceImpl trackedRace) {
        
        TrackedRaceHashForMarkPassingCalculationImpl hashValues = new TrackedRaceHashForMarkPassingCalculationImpl(trackedRace.getHashValuesForMarkPassingCalculation());
        return hashValues;
    }
}
