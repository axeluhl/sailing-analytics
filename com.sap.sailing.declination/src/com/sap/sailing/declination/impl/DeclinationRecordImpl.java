package com.sap.sailing.declination.impl;

import com.sap.sailing.declination.DeclinationRecord;
import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;

public class DeclinationRecordImpl implements DeclinationRecord {
    private final Position position;
    private final TimePoint timePoint;
    private final Bearing bearing;
    private final Bearing annualChange;
    
    public DeclinationRecordImpl(Position position, TimePoint timePoint, Bearing bearing, Bearing annualChange) {
        super();
        this.position = position;
        this.timePoint = timePoint;
        this.bearing = bearing;
        this.annualChange = annualChange;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public TimePoint getTimePoint() {
        return timePoint;
    }

    @Override
    public Bearing getBearing() {
        return bearing;
    }

    @Override
    public Bearing getAnnualChange() {
        return annualChange;
    }

}
