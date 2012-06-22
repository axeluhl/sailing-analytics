package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.impl.AbstractSpeedWithAbstractBearingImpl;
import com.sap.sailing.domain.base.impl.AbstractTimePoint;
import com.sap.sailing.domain.common.AbstractBearing;
import com.sap.sailing.domain.common.AbstractPosition;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.Wind;

public class CompactWindImpl extends AbstractSpeedWithAbstractBearingImpl implements Wind {
    private static final long serialVersionUID = -5059956032663387929L;
    private final double latDeg;
    private final double lngDeg;
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
        this.degBearing = wind.getBearing().getDegrees();
        this.latDeg = wind.getPosition().getLatDeg();
        this.lngDeg = wind.getPosition().getLngDeg();
        this.speedInKnots = wind.getKnots();
        this.timePointAsMillis = wind.getTimePoint().asMillis();
    }

    @Override
    public Position getPosition() {
        return new CompactPosition();
    }

    @Override
    public TimePoint getTimePoint() {
        return new CompactTimePoint();
    }

    @Override
    public Bearing getBearing() {
        return new CompactBearing();
    }

    @Override
    public double getKnots() {
        return speedInKnots;
    }

    @Override
    public Bearing getFrom() {
        return getBearing().reverse();
    }

}
