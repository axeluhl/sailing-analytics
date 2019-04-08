package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.common.CourseChange;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.AbstractSpeedWithAbstractBearingImpl;
import com.sap.sailing.domain.common.impl.AbstractSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.KilometersPerHourSpeedImpl;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;

public class KilometersPerHourSpeedWithBearingImpl extends KilometersPerHourSpeedImpl implements SpeedWithBearing {
    private static final long serialVersionUID = 5749611820871048625L;
    private final Bearing bearing;
    
    public KilometersPerHourSpeedWithBearingImpl(double speedInKilometersPerHour, Bearing bearing) {
        super(speedInKilometersPerHour);
        this.bearing = bearing;
    }

    @Override
    public Bearing getBearing() {
        return bearing;
    }

    @Override
    public Position travelTo(Position pos, TimePoint from, TimePoint to) {
        return pos.translateGreatCircle(getBearing(), this.travel(from, to));
    }

    @Override
    public SpeedWithBearing add(SpeedWithBearing other) {
        return AbstractSpeedWithBearingImpl.add(this, other);
    }

    @Override
    public SpeedWithBearing applyCourseChange(CourseChange courseChange) {
        return AbstractSpeedWithBearingImpl.applyCourseChange(this, courseChange);
    }

    @Override
    public CourseChange getCourseChangeRequiredToReach(SpeedWithBearing targetSpeedWithBearing) {
        return AbstractSpeedWithBearingImpl.getCourseChangeRequiredToReach(this, targetSpeedWithBearing);
    }

    @Override
    public String toString() {
        return super.toString()+" to "+getBearing().getDegrees()+"°";
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ getBearing().hashCode();
    }
    
    @Override
    public boolean equals(Object object) {
        return super.equals(object) && object instanceof SpeedWithBearing
                && getBearing().equals(((SpeedWithBearing) object).getBearing());
    }

    @Override
    public Speed projectTo(Position position, Bearing projectTo) {
        return AbstractSpeedWithAbstractBearingImpl.projectTo(this, position, projectTo);
    }
}
