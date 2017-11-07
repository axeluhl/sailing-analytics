package com.sap.sailing.domain.tracking.impl;

import java.io.Serializable;
import java.util.function.Function;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.confidence.impl.ScalableDouble;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalableBearing;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalableDistance;
import com.sap.sailing.domain.common.tracking.BravoExtendedFix;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sailing.domain.tracking.DynamicBravoFixTrack;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.WithID;
import com.sap.sse.common.scalablevalue.ScalableValue;

/**
 * Specific {@link SensorFixTrackImpl} used for {@link BravoFix}es.
 *
 * @param <ItemType> the type of item this track is mapped to
 */
public class BravoFixTrackImpl<ItemType extends WithID & Serializable> extends SensorFixTrackImpl<ItemType, BravoFix>
        implements DynamicBravoFixTrack<ItemType> {
    private static final long serialVersionUID = 460944392510182976L;
    
    private final boolean hasExtendedFixes;

    /**
     * @param trackedItem
     *            the item this track is mapped to
     * @param trackName
     *            the name of the track by which it can be obtained from the {@link TrackedRace}.
     */
    public BravoFixTrackImpl(ItemType trackedItem, String trackName, boolean hasExtendedFixes) {
        super(trackedItem, trackName, BravoFixTrack.TRACK_NAME + " for " + trackedItem);
        this.hasExtendedFixes = hasExtendedFixes;
    }

    @Override
    public Distance getRideHeight(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoFix::getRideHeight,
                ScalableDistance::new);
    }

    @Override
    public Bearing getHeel(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoFix::getHeel,
                ScalableBearing::new);
    }

    @Override
    public Bearing getPitch(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoFix::getPitch,
                ScalableBearing::new);
    }
    
    @Override
    public boolean isFoiling(TimePoint timePoint) {
        final Distance rideHeight = getRideHeight(timePoint);
        return rideHeight != null && rideHeight.compareTo(BravoFix.MIN_FOILING_HEIGHT_THRESHOLD) >= 0;
    }

    @Override
    public Distance getAverageRideHeight(TimePoint from, TimePoint to) {
        final Distance result;
        lockForRead();
        try {
            Distance sum = Distance.NULL;
            int count = 0;
            for (final BravoFix fix : getFixes(from, true, to, true)) {
                sum = sum.add(fix.getRideHeight());
                count++;
            }
            if (count > 0) {
                result = sum.scale(1./count);
            } else {
                result = null;
            }
        } finally {
            unlockAfterRead();
        }
        return result;
    }
    
    private boolean isFoiling(BravoFix fix) {
        return fix.isFoiling();
    }

    @Override
    public Duration getTimeSpentFoiling(TimePoint from, TimePoint to) {
        Duration result = Duration.NULL;
        lockForRead();
        try {
            TimePoint last = from;
            boolean isFoiling = false;
            for (final BravoFix fix : getFixes(from, true, to, true)) {
                final boolean fixFoils = isFoiling(fix);
                if (isFoiling && fixFoils) {
                    result = result.plus(last.until(fix.getTimePoint()));
                }
                last = fix.getTimePoint();
                isFoiling = fixFoils;
            }
        } finally {
            unlockAfterRead();
        }
        return result;
    }

    @Override
    public Distance getDistanceSpentFoiling(GPSFixTrack<Competitor, GPSFixMoving> gpsFixTrack, TimePoint from, TimePoint to) {
        Distance result = Distance.NULL;
        lockForRead();
        try {
            TimePoint last = from;
            boolean isFoiling = false;
            for (final BravoFix fix : getFixes(from, true, to, true)) {
                final boolean fixFoils = isFoiling(fix);
                if (isFoiling && fixFoils) {
                    result = result.add(gpsFixTrack.getDistanceTraveled(last, fix.getTimePoint()));
                }
                last = fix.getTimePoint();
                isFoiling = fixFoils;
            }
        } finally {
            unlockAfterRead();
        }
        return result;
    }

    @Override
    public boolean hasExtendedFixes() {
        return hasExtendedFixes;
    }
    
    /**
     * Generic implementation to get values from extended fixes. The implementation ensured that in case of simple
     * {@link BravoFix} instances, just {@code null} is returned. If valid {@link BravoExtendedFix BravoExtendedFixes}
     * are found, the provided getter is used to extract the value from the identified fix.
     * <p>
     * 
     * In case of a mix of {@link BravoFix} and {@link BravoExtendedFix} instances, this method may return null values
     * for specific {@link TimePoint TimePoints}.
     * <p>
     * 
     * If the value extracted by the {@code getter} is {@code null}, the next fix will be probed, until no more fix
     * exists in that direction or a fix is found that delivers a non-{@code null} value for the {@code getter} result.
     * This way it is possible to skip fixes that don't make a statement with regard to the attribute extracted by the
     * {@code getter}.
     */
    private <T, I, BravoFixType extends BravoFix> T getValueFromExtendedFixSkippingNullValues(
            final TimePoint timePoint, final Function<BravoFixType, T> getter,
            Function<T, ScalableValue<I, T>> converterToScalableValue) {
        final com.sap.sse.common.Util.Function<BravoFix, ScalableValue<I, T>> converter =
              fix -> {
                  @SuppressWarnings("unchecked")
                final BravoFixType castFix = (BravoFixType) fix;
                  return converterToScalableValue.apply(getter.apply(castFix));  
              };
        return getInterpolatedValue(timePoint, converter, fix->{
            @SuppressWarnings("unchecked")
            final BravoFixType castFix = (BravoFixType) fix;
            return getter.apply(castFix) != null;
        });
    }
    
    public BravoExtendedFix getFirstFixAtOrAfterIfExtended(TimePoint timePoint) {
        BravoFix fix = getFirstFixAtOrAfter(timePoint);
        return fix instanceof BravoExtendedFix ? (BravoExtendedFix) fix : null;
    }
    
    public BravoExtendedFix getLastFixAtOrBeforeIfExtended(TimePoint timePoint) {
        BravoFix fix = getLastFixAtOrBefore(timePoint);
        return fix instanceof BravoExtendedFix ? (BravoExtendedFix) fix : null;
    }

    @Override
    public Double getPortDaggerboardRakeIfAvailable(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoExtendedFix::getPortDaggerboardRake,
                ScalableDouble::new);
    }

    @Override
    public Double getStbdDaggerboardRakeStbdIfAvailable(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoExtendedFix::getStbdDaggerboardRake,
                ScalableDouble::new);
    }

    @Override
    public Double getPortRudderRakeIfAvailable(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoExtendedFix::getPortRudderRake,
                ScalableDouble::new);
    }

    @Override
    public Double getStbdRudderRakeIfAvailable(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoExtendedFix::getStbdRudderRake,
                ScalableDouble::new);
    }

    @Override
    public Bearing getMastRotationIfAvailable(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoExtendedFix::getMastRotation,
                NaivelyScalableBearing::new);
    }
    
    @Override
    public Bearing getLeewayIfAvailable(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoExtendedFix::getLeeway,
                NaivelyScalableBearing::new);
    }

    @Override
    public Double getSetIfAvailable(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoExtendedFix::getSet,
                ScalableDouble::new);
    }

    @Override
    public Bearing getDriftIfAvailable(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoExtendedFix::getDrift,
                NaivelyScalableBearing::new);
    }

    @Override
    public Distance getDepthIfAvailable(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoExtendedFix::getDepth,
                ScalableDistance::new);
    }

    @Override
    public Bearing getRudderIfAvailable(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoExtendedFix::getRudder,
                NaivelyScalableBearing::new);
    }

    @Override
    public Double getForestayLoadIfAvailable(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoExtendedFix::getForestayLoad,
                ScalableDouble::new);
    }

    @Override
    public Double getForestayPressureIfAvailable(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoExtendedFix::getForestayPressure,
                ScalableDouble::new);
    }

    @Override
    public Bearing getTackAngleIfAvailable(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoExtendedFix::getTackAngle,
                NaivelyScalableBearing::new);
    }

    @Override
    public Bearing getRakeIfAvailable(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoExtendedFix::getRake,
                NaivelyScalableBearing::new);
    }

    @Override
    public Double getDeflectorPercentageIfAvailable(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoExtendedFix::getDeflectorPercentage,
                ScalableDouble::new);
    }

    @Override
    public Bearing getTargetHeelIfAvailable(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoExtendedFix::getTargetHeel,
                NaivelyScalableBearing::new);
    }

    @Override
    public Distance getDeflectorIfAvailable(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoExtendedFix::getDeflector,
                ScalableDistance::new);
    }

    @Override
    public Double getTargetBoatspeedPIfAvailable(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoExtendedFix::getTargetBoatspeedP,
                ScalableDouble::new);
    }

    private static class NaivelyScalableBearing implements ScalableValue<Double, Bearing> {
        private final double deg;
        
        public NaivelyScalableBearing(Bearing b) {
            deg = b.getDegrees();
        }
        
        private NaivelyScalableBearing(double deg) {
            this.deg = deg;
        }
        
        @Override
        public ScalableValue<Double, Bearing> multiply(double factor) {
            return new NaivelyScalableBearing(deg*factor);
        }

        @Override
        public ScalableValue<Double, Bearing> add(ScalableValue<Double, Bearing> t) {
            return new NaivelyScalableBearing(deg+t.getValue());
        }

        @Override
        public Bearing divide(double divisor) {
            return new DegreeBearingImpl(deg/divisor);
        }

        @Override
        public Double getValue() {
            return deg;
        }
    }
}