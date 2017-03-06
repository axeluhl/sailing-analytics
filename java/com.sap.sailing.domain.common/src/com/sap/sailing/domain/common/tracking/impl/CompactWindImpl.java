package com.sap.sailing.domain.common.tracking.impl;

import com.sap.sailing.domain.common.AbstractBearing;
import com.sap.sailing.domain.common.AbstractPosition;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.AbstractSpeedWithAbstractBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.AbstractTimePoint;

public class CompactWindImpl extends AbstractSpeedWithAbstractBearingImpl implements Wind {
    private static final long serialVersionUID = -5059956032663387929L;
    /**
     * See {@link CompactPositionHelper}
     */
    private final int latDegScaled;

    /**
     * See {@link CompactPositionHelper}
     */
    private final int lngDegScaled;
    
    /**
     * bit mask for {@link #flags}, telling the bit encoding whether the position is {@code null}.
     */
    private static final byte POSITION_IS_NULL = 1<<0;

    /**
     * bit mask for {@link #flags}, telling the bit encoding whether the bearing is {@code null}.
     */
    private static final byte BEARING_IS_NULL = 1<<1;

    /**
     * bit mask for {@link #flags}, telling the bit encoding whether the time point is {@code null}.
     */
    private static final byte TIME_POINT_IS_NULL = 1<<2;
    
    /**
     * See the bit mask values such as {@link #POSITION_IS_NULL}, {@link #BEARING_IS_NULL} and {@link #TIME_POINT_IS_NULL}.
     */
    private final byte flags;
    
    /**
     * See {@link CompactPositionHelper}
     */
    private final short speedInKnotsScaled;

    /**
     * See {@link CompactPositionHelper}
     */
    private final short degreeBearingScaled;
    
    private final long timePointAsMillis;

    public class CompactPosition extends AbstractPosition {
        private static final long serialVersionUID = 5621506820766614178L;

        @Override
        public double getLatDeg() {
            return CompactPositionHelper.getLatDeg(latDegScaled);
        }

        @Override
        public double getLngDeg() {
            return CompactPositionHelper.getLngDeg(lngDegScaled);
        }
    }

    private class CompactBearing extends AbstractBearing {
        private static final long serialVersionUID = -6474909210513108635L;

        @Override
        public double getDegrees() {
            return CompactPositionHelper.getDegreeBearing(degreeBearingScaled);
        }

        @Override
        public double getRadians() {
            return getDegrees() / 180. * Math.PI;
        }

        @Override
        public boolean equals(Object object) {
            return this==object || object instanceof Bearing && getDegrees() == ((Bearing) object).getDegrees();
        }
    }

    private class CompactTimePoint extends AbstractTimePoint implements TimePoint {
        private static final long serialVersionUID = -2470922642359937437L;

        @Override
        public long asMillis() {
            return timePointAsMillis;
        }
    }
    
    public CompactWindImpl(Wind wind) throws CompactionNotPossibleException {
        final boolean bearingIsNull;
        final boolean positionIsNull;
        final boolean timePointIsNull;
        if (wind.getBearing() == null) {
            bearingIsNull = true;
            degreeBearingScaled = 0;
        } else {
            this.degreeBearingScaled = CompactPositionHelper.getDegreeBearingScaled(wind.getBearing());
            bearingIsNull = false;
        }
        if (wind.getPosition() == null) {
            positionIsNull = true;
            this.latDegScaled = 0;
            this.lngDegScaled = 0;
        } else {
            this.latDegScaled = CompactPositionHelper.getLatDegScaled(wind.getPosition());
            this.lngDegScaled = CompactPositionHelper.getLngDegScaled(wind.getPosition());
            positionIsNull = false;
        }
        this.speedInKnotsScaled = CompactPositionHelper.getKnotSpeedScaled(wind);
        if (wind.getTimePoint() == null) {
            timePointIsNull = true;
            this.timePointAsMillis = 0;
        } else {
            timePointIsNull = false;
            this.timePointAsMillis = wind.getTimePoint().asMillis();
        }
        flags = (byte) ((byte) (bearingIsNull ? BEARING_IS_NULL : (byte) 0) | (positionIsNull ? POSITION_IS_NULL : (byte) 0) | (timePointIsNull ? TIME_POINT_IS_NULL : (byte) 0));
    }

    @Override
    public Position getPosition() {
        if ((flags & POSITION_IS_NULL) != 0) {
            return null;
        } else {
            return new CompactPosition();
        }
    }

    @Override
    public TimePoint getTimePoint() {
        if ((flags & TIME_POINT_IS_NULL) != 0) {
            return null;
        } else {
            return new CompactTimePoint();
        }
    }

    @Override
    public Bearing getBearing() {
        if ((flags & BEARING_IS_NULL) != 0) {
            return null;
        } else {
            return new CompactBearing();
        }
    }

    @Override
    public double getKnots() {
        return CompactPositionHelper.getKnotSpeed(speedInKnotsScaled);
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
    public int hashCode() {
        return WindImpl.hashCode(getPosition().getLatDeg(), getPosition().getLngDeg(), (flags&TIME_POINT_IS_NULL) != 0?0:timePointAsMillis);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof Wind))
            return false;
        Wind other = (Wind) obj;
        return Util.equalsWithNull(getPosition(), other.getPosition()) && Util.equalsWithNull(getTimePoint(), other.getTimePoint());
    }

    @Override
    public String toString() {
        return ""+getTimePoint()+"@"+getPosition()+": "+getKnots()+"kn from "+getFrom();
    }
}
