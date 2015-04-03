package com.sap.sailing.domain.common.tracking.impl;

import com.sap.sailing.domain.common.AbstractBearing;
import com.sap.sailing.domain.common.AbstractPosition;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.AbstractSpeedWithAbstractBearingImpl;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.AbstractTimePoint;

/**
 * A compact representation of a GPS fix which collects all primitive-typed attributes in one object to avoid
 * memory overhead otherwise created by using too many individual fine-grained objects.<p>
 * 
 * Objects of this type are assumed to be contained in at most one {@link DynamicGPSFixTrackImpl}. It is
 * therefore permissible to cache information about validity to speed up the otherwise expensive
 * {@link DynamicGPSFixTrackImpl#isValid(PartialNavigableSetView, GPSFix)} computation.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CompactGPSFixImpl extends AbstractGPSFixImpl {
    private static final long serialVersionUID = 8167588584536992501L;
    
    /**
     * Bit mask for {@link #whatIsCached}, telling whether validity is currently cached
     */
    private static final byte IS_VALIDITY_CACHED = 1<<0;

    /**
     * Bit mask for {@link #whatIsCached}, telling whether the estimated speed is currently cached
     */
    private static final byte IS_ESTIMATED_SPEED_CACHED = 1<<1;
    
    /**
     * Bit mask for {@link #whatIsCached}, telling the validity of the fix; only relevant if
     * <code>{@link #whatIsCached}&amp;{@link #IS_VALIDITY_CACHED} != 0</code>
     */
    private static final byte VALIDITY = 1<<2;
    
    private final double latDeg;
    private final double lngDeg;
    private final long timePointAsMillis;
    
    /**
     * Tells if in the containing {@link DynamicGPSFixTrackImpl} this fix is considered valid. This cache
     * needs to be invalidated as soon as fixes are added to the containing track which may have an impact
     * on this fix's validity. See the bitmask values such as {@link #VALIDITY}, {@link #IS_ESTIMATED_SPEED_CACHED} and
     * {@link #IS_VALIDITY_CACHED}.
     */
    private byte whatIsCached = 0;
    
    /**
     * When <code>{@link #whatIsCached}&amp;{@link #IS_ESTIMATED_SPEED_CACHED} != 0</code>, this field tells the estimated speed's
     * true "bearing" (true course over ground) in degrees.
     */
    private double cachedEstimatedSpeedBearingInDegrees;

    /**
     * When <code>{@link #whatIsCached}&amp;{@link #IS_ESTIMATED_SPEED_CACHED} != 0</code>, this field tells the estimated speed
     * in knots.
     */
    private double cachedEstimatedSpeedInKnots;
    
    public class CompactPosition extends AbstractPosition {
        private static final long serialVersionUID = 5621506820766614178L;
        CompactPosition() {} // for GWT serialization

        @Override
        public double getLatDeg() {
            return latDeg;
        }

        @Override
        public double getLngDeg() {
            return lngDeg;
        }
    }
    
    private class CompactTimePoint extends AbstractTimePoint implements TimePoint {
        private static final long serialVersionUID = -2470922642359937437L;

        @Override
        public long asMillis() {
            return timePointAsMillis;
        }
    }
    
    private class CompactEstimatedSpeedBearing extends AbstractBearing {
        private static final long serialVersionUID = 8549231429037883121L;

        @Override
        public double getDegrees() {
            return cachedEstimatedSpeedBearingInDegrees;
        }
    }
    
    private class CompactEstimatedSpeed extends AbstractSpeedWithAbstractBearingImpl {
        private static final long serialVersionUID = -5871855443391817248L;

        @Override
        public Bearing getBearing() {
            return new CompactEstimatedSpeedBearing();
        }

        @Override
        public double getKnots() {
            return cachedEstimatedSpeedInKnots;
        }
    }
    
    CompactGPSFixImpl() { latDeg=0; lngDeg=0; timePointAsMillis=0; } // for GWT serialization only
    
    public CompactGPSFixImpl(Position position, TimePoint timePoint) {
        latDeg = position.getLatDeg();
        lngDeg = position.getLngDeg();
        timePointAsMillis = timePoint.asMillis();
    }
    
    public CompactGPSFixImpl(GPSFix gpsFix) {
        this(gpsFix.getPosition(), gpsFix.getTimePoint());
    }

    @Override
    public String toString() {
        return getTimePoint() + ": " + getPosition();
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
    public boolean isValidityCached() {
        return (whatIsCached & IS_VALIDITY_CACHED) != 0;
    }

    @Override
    public boolean isValidCached() {
        assert isValidityCached();
        return (whatIsCached & VALIDITY) != 0;
    }

    @Override
    public void invalidateCache() {
        whatIsCached &= ~IS_VALIDITY_CACHED;
    }

    @Override
    public void cacheValidity(boolean isValid) {
        if (isValid) {
            whatIsCached |= IS_VALIDITY_CACHED | VALIDITY;
        } else {
            whatIsCached |= IS_VALIDITY_CACHED;
            whatIsCached &= ~VALIDITY;
        }
    }

    @Override
    public boolean isEstimatedSpeedCached() {
        return (whatIsCached & IS_ESTIMATED_SPEED_CACHED) != 0;
    }

    @Override
    public SpeedWithBearing getCachedEstimatedSpeed() {
        assert isEstimatedSpeedCached();
        return new CompactEstimatedSpeed();
    }

    @Override
    public void invalidateEstimatedSpeedCache() {
        whatIsCached &= ~IS_ESTIMATED_SPEED_CACHED;
    }

    @Override
    public void cacheEstimatedSpeed(SpeedWithBearing estimatedSpeed) {
        cachedEstimatedSpeedBearingInDegrees = estimatedSpeed.getBearing().getDegrees();
        cachedEstimatedSpeedInKnots = estimatedSpeed.getKnots();
        whatIsCached |= IS_ESTIMATED_SPEED_CACHED;
    }
    
    
}
