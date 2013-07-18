package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.GPSFixContext;
import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;

public class GPSFixWithContextImpl extends GPSFixMovingImpl implements GPSFixWithContext {
    private static final long serialVersionUID = -7871244536135981633L;
    
    private GPSFixContext context;

    public GPSFixWithContextImpl(GPSFixMoving gpsFix, GPSFixContext context) {
        this(gpsFix.getPosition(), gpsFix.getTimePoint(), gpsFix.getSpeed(), context);
    }

    public GPSFixWithContextImpl(Position position, TimePoint timePoint, SpeedWithBearing speed, GPSFixContext context) {
        super(position, timePoint, speed);
        this.context = context;
    }

    @Override
    public Competitor getCompetitor() {
        return context.getCompetitor();
    }

    @Override
    public LegType getLegType() {
        TrackedLegOfCompetitor currentLeg = getRace().getCurrentLeg(getCompetitor(), getTimePoint());
        try {
            return currentLeg == null ? null : currentLeg.getTrackedLeg().getLegType(getTimePoint());
        } catch (NoWindException e) {
            return null;
        }
    }

    @Override
    public TrackedRace getRace() {
        return context.getTrackedRace();
    }

    @Override
    public Regatta getRegatta() {
        return getRace().getTrackedRegatta().getRegatta();
    }
    
    @Override
    public CourseArea getCourseArea() {
        return getRegatta().getDefaultCourseArea();
    }

}
