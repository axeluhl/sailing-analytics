package com.sap.sailing.domain.tracking.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.function.Function;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.confidence.impl.ScalableDouble;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalableDistance;
import com.sap.sailing.domain.common.tracking.BravoExtendedFix;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sailing.domain.tracking.DynamicBravoFixTrack;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.GPSTrackListener;
import com.sap.sailing.domain.tracking.Track;
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
    
    private transient TimeRangeCache<Duration> foilingTimeCache;
    
    private transient TimeRangeCache<Distance> foilingDistanceCache;
    
    private transient TimeRangeCache<Distance> averageRideHeightCache;
    
    /**
     * If a GPS track was provided at construction time, remember it non-transiently. It is needed when restoring
     * the object after de-serialization, so the cache invalidation listener can be re-established.
     */
    private GPSFixTrack<ItemType, GPSFixMoving> gpsTrack;
    
    private class CacheInvalidationGpsTrackListener implements GPSTrackListener<ItemType, GPSFixMoving> {
        private static final long serialVersionUID = 6395529765232404414L;
        @Override
        public boolean isTransient() {
            return true;
        }

        @Override
        public void gpsFixReceived(GPSFixMoving fix, ItemType item, boolean firstFixInTrack) {
            assert item == getTrackedItem();
            foilingDistanceCache.invalidateAllAtOrLaterThan(fix.getTimePoint());
        }

        @Override
        public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage,
                long newMillisecondsOverWhichToAverage) {
        }
    }

    /**
     * @param trackedItem
     *            the item this track is mapped to
     * @param trackName
     *            the name of the track by which it can be obtained from the {@link TrackedRace}.
     */
    public BravoFixTrackImpl(ItemType trackedItem, String trackName, boolean hasExtendedFixes) {
        this(trackedItem, trackName, hasExtendedFixes, /* listen to GPS track for distance cache invalidation */ null);
    }
    
    /**
     * @param gpsTrack
     *            if not {@code null}, this track will listen on that track for changes in order to invalidate this
     *            track's caches; for example, if a GPS fix is added, this track may need to invalidate its
     *            {@link #foilingDistanceCache} accordingly because the distance traveled between the fixes may have
     *            changed.
     */
    public BravoFixTrackImpl(ItemType trackedItem, String trackName, boolean hasExtendedFixes, GPSFixTrack<ItemType, GPSFixMoving> gpsTrack) {
        super(trackedItem, trackName, BravoFixTrack.TRACK_NAME + " for " + trackedItem);
        this.hasExtendedFixes = hasExtendedFixes;
        this.foilingTimeCache = createFoilingTimeCache(trackedItem);
        this.foilingDistanceCache = createFoilingDistanceCache(trackedItem);
        this.averageRideHeightCache = createAverageRideHeightCache(trackedItem);
        this.gpsTrack = gpsTrack;
        if (gpsTrack != null) {
            gpsTrack.addListener(new CacheInvalidationGpsTrackListener());
        }
    }

    private TimeRangeCache<Distance> createAverageRideHeightCache(ItemType trackedItem) {
        return createTimeRangeCache(trackedItem, "averageRideHeightCache");
    }

    private <T> TimeRangeCache<T> createTimeRangeCache(ItemType trackedItem, final String cacheName) {
        return new TimeRangeCache<>(cacheName+" for "+trackedItem);
    }

    private TimeRangeCache<Distance> createFoilingDistanceCache(ItemType trackedItem) {
        return createTimeRangeCache(trackedItem, "foilingDistanceCache");
    }

    private TimeRangeCache<Duration> createFoilingTimeCache(ItemType trackedItem) {
        return createTimeRangeCache(trackedItem, "foilingTimeCache");
    }
    
    /**
     * After reading this object from an {@link ObjectInputStream}, initialize the caches properly.
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        this.foilingTimeCache = createFoilingTimeCache(getTrackedItem());
        this.foilingDistanceCache = createFoilingDistanceCache(getTrackedItem());
        this.averageRideHeightCache = createAverageRideHeightCache(getTrackedItem());
        if (gpsTrack != null) {
            gpsTrack.addListener(new CacheInvalidationGpsTrackListener());
        }
    }

    @Override
    public Distance getRideHeight(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoFix::getRideHeight,
                ScalableDistance::new);
    }

    @Override
    public Bearing getHeel(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoFix::getHeel,
                NaivelyScalableBearing::new);
    }

    @Override
    public Bearing getPitch(TimePoint timePoint) {
        return getValueFromExtendedFixSkippingNullValues(timePoint, BravoFix::getPitch,
                NaivelyScalableBearing::new);
    }
    
    @Override
    public boolean isFoiling(TimePoint timePoint) {
        final Distance rideHeight = getRideHeight(timePoint);
        return rideHeight != null && rideHeight.compareTo(BravoFix.MIN_FOILING_HEIGHT_THRESHOLD) >= 0;
    }

    
    @Override
    public boolean add(BravoFix fix, boolean replace) {
        final boolean added = super.add(fix, replace);
        if (added) {
            final TimePoint fixTimePoint = fix.getTimePoint();
            averageRideHeightCache.invalidateAllAtOrLaterThan(fixTimePoint);
            foilingDistanceCache.invalidateAllAtOrLaterThan(fixTimePoint);
            foilingTimeCache.invalidateAllAtOrLaterThan(fixTimePoint);
        }
        return added;
    }

    @Override
    public Distance getAverageRideHeight(TimePoint from, TimePoint to) {
        return getValueSum(from, to, /* nullElement */ Distance.NULL, Distance::add, averageRideHeightCache,
                /* valueCalculator */ new Track.TimeRangeValueCalculator<Distance>() {
            @Override
            public Distance calculate(TimePoint from, TimePoint to) {
                Distance result;
                Distance sum = Distance.NULL;
                int count = 0;
                for (final BravoFix fix : getFixes(from, true, to, true)) {
                    final Distance rideHeight = fix.getRideHeight();
                    if (rideHeight != null) {
                        sum = sum.add(rideHeight);
                        count++;
                    }
                }
                if (count > 0) {
                    result = sum.scale(1./count);
                } else {
                    result = null;
                }
                return result;
            }
        });
    }
    
    private boolean isFoiling(BravoFix fix) {
        return fix.isFoiling();
    }

    @Override
    public Duration getTimeSpentFoiling(TimePoint from, TimePoint to) {
        return getValueSum(from, to, /* nullElement */ Duration.NULL, Duration::plus, foilingTimeCache,
                /* valueCalculator */ new Track.TimeRangeValueCalculator<Duration>() {
            @Override
            public Duration calculate(TimePoint from, TimePoint to) {
                Duration result = Duration.NULL;
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
                return result;
            }
        });
    }

    @Override
    public Distance getDistanceSpentFoiling(TimePoint from, TimePoint to) {
        assert gpsTrack != null;
        return getValueSum(from, to, /* nullElement */ Distance.NULL, Distance::add, foilingDistanceCache,
                /* valueCalculator */ new Track.TimeRangeValueCalculator<Distance>() {
            @Override
            public Distance calculate(TimePoint from, TimePoint to) {
                Distance result = Distance.NULL;
                TimePoint last = from;
                boolean isFoiling = false;
                for (final BravoFix fix : getFixes(from, true, to, true)) {
                    final boolean fixFoils = isFoiling(fix);
                    if (isFoiling && fixFoils) {
                        result = result.add(gpsTrack.getDistanceTraveled(last, fix.getTimePoint()));
                    }
                    last = fix.getTimePoint();
                    isFoiling = fixFoils;
                }
                return result;
            }
        });
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