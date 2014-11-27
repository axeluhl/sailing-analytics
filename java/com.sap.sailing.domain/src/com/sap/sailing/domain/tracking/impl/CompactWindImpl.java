package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.common.AbstractBearing;
import com.sap.sailing.domain.common.AbstractPosition;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.AbstractSpeedWithAbstractBearingImpl;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.AbstractTimePoint;

public class CompactWindImpl extends AbstractSpeedWithAbstractBearingImpl implements Wind {
    private static final long serialVersionUID = -5059956032663387929L;
    private final double latDeg;
    private final double lngDeg;
    private final boolean positionIsNull;
    private final boolean bearingIsNull;
    private final boolean timePointIsNull;
    private final double speedInKnots;
    private final double degBearing;
    private final long timePointAsMillis;

    private class CompactPosition extends AbstractPosition {
        private static final long serialVersionUID = 5621506820766614178L;

        @Override
        public double getLatDeg() {
            return latDeg;
        }

        @Override
        public double getLngDeg() {
            return lngDeg;
        }
    }

    private class CompactBearing extends AbstractBearing {
        private static final long serialVersionUID = -6474909210513108635L;

        @Override
        public double getDegrees() {
            return degBearing;
        }

        @Override
        public double getRadians() {
            return degBearing / 180. * Math.PI;
        }
    }

    private class CompactTimePoint extends AbstractTimePoint implements TimePoint {
        private static final long serialVersionUID = -2470922642359937437L;

        @Override
        public long asMillis() {
            return timePointAsMillis;
        }
    }
    
    public CompactWindImpl(Wind wind) {
        if (wind.getBearing() == null) {
            bearingIsNull = true;
            degBearing = 0;
        } else {
            this.degBearing = wind.getBearing().getDegrees();
            bearingIsNull = false;
        }
        if (wind.getPosition() == null) {
            positionIsNull = true;
            this.latDeg = 0;
            this.lngDeg = 0;
        } else {
            this.latDeg = wind.getPosition().getLatDeg();
            this.lngDeg = wind.getPosition().getLngDeg();
            positionIsNull = false;
        }
        this.speedInKnots = wind.getKnots();
        if (wind.getTimePoint() == null) {
            timePointIsNull = true;
            this.timePointAsMillis = 0;
        } else {
            timePointIsNull = false;
            this.timePointAsMillis = wind.getTimePoint().asMillis();
        }
    }

    @Override
    public Position getPosition() {
        if (positionIsNull) {
            return null;
        } else {
            return new CompactPosition();
        }
    }

    @Override
    public TimePoint getTimePoint() {
        if (timePointIsNull) {
            return null;
        } else {
            return new CompactTimePoint();
        }
    }

    @Override
    public Bearing getBearing() {
        if (bearingIsNull) {
            return null;
        } else {
            return new CompactBearing();
        }
    }

    @Override
    public double getKnots() {
        return speedInKnots;
    }

    @Override
    public Bearing getFrom() {
        if (getBearing() == null) {
            return null;
        } else {
            return getBearing().reverse();
        }
    }

    @Override
    public String toString() {
        return ""+getTimePoint()+"@"+getPosition()+": "+getKnots()+"kn from "+getFrom();
    }
}
