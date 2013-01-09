package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.impl.AbstractTimePoint;
import com.sap.sailing.domain.common.AbstractPosition;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.GPSFix;

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
    private final double latDeg;
    private final double lngDeg;
    private final long timePointAsMillis;
    
    /**
     * Tells if in the containing {@link DynamicGPSFixTrackImpl} this fix is considered valid. This cache
     * needs to be invalidated as soon as fixes are added to the containing track which may have an impact
     * on this fix's validity. -1 means "no value"; 0 means invalid, 1 means valid.
     */
    private byte validityCache = -1;
    
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
    
    private class CompactTimePoint extends AbstractTimePoint implements TimePoint {
        private static final long serialVersionUID = -2470922642359937437L;

        @Override
        public long asMillis() {
            return timePointAsMillis;
        }
    }
    
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
        return validityCache != -1;
    }

    @Override
    public boolean isValid() {
        return validityCache == 1;
    }

    @Override
    public void invalidateCache() {
        validityCache = -1;
    }

    @Override
    public void cacheValidity(boolean isValid) {
        validityCache = (byte) (isValid ? 1 : 0);
    }
    
}
