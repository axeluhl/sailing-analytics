package com.sap.sailing.declination.impl;

import com.sap.sailing.declination.DeclinationRecord;
import com.sap.sailing.declination.DeclinationService;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;

public class DeclinationServiceImpl implements DeclinationService {
    private final Distance defaultMaxDistance;
    
    /**
     * Constructs a service that has a default position tolerance of <code>defaultMaxDistance</code>.
     */
    public DeclinationServiceImpl(Distance defaultMaxDistance) {
        this.defaultMaxDistance = defaultMaxDistance;
    }

    @Override
    public DeclinationRecord getDeclination(TimePoint timePoint, Position position) {
        return getDeclination(timePoint, position, defaultMaxDistance);
    }

    @Override
    public DeclinationRecord getDeclination(TimePoint timePoint, Position position, Distance maxDistance) {
        // TODO Auto-generated method stub
        return null;
    }

}
