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
import com.sap.sse.common.Util.Pair;
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
    
    /**
     * Caches the sum of the ride height of all fixes in the interval, paired with the number of fixes
     * that constituted the basis for the ride height sum.
     */
    private transient TimeRangeCache<Pair<Distance, Long>> averageRideHeightCache;
    
    private transient TimeRangeCache<Pair<Double, Long>> expeditionAWACache;
    private transient TimeRangeCache<Pair<Double, Long>> expeditionAWSCache;
    private transient TimeRangeCache<Pair<Double, Long>> expeditionTWACache;
    private transient TimeRangeCache<Pair<Double, Long>> expeditionTWSCache;
    private transient TimeRangeCache<Pair<Double, Long>> expeditionTWDCache;
    private transient TimeRangeCache<Pair<Double, Long>> expeditionBoatSpeedCache;
    private transient TimeRangeCache<Pair<Double, Long>> expeditionTargBoatSpeedCache;
    private transient TimeRangeCache<Pair<Double, Long>> expeditionSOGCache;
    private transient TimeRangeCache<Pair<Double, Long>> expeditionCOGCache;
    private transient TimeRangeCache<Pair<Double, Long>> expeditionForestayLoadCache;
    private transient TimeRangeCache<Pair<Double, Long>> expeditionRakeCache;
    private transient TimeRangeCache<Pair<Double, Long>> expeditionCourseDetailCache;
    
    /**
     * If a GPS track was provided at construction time, remember it non-transiently. It is needed when restoring
     * the object after de-serialization, so the cache invalidation listener can be re-established.
     */
    private transient GPSFixTrack<ItemType, GPSFixMoving> gpsTrack;
    
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
        initCaches(trackedItem);
        
        setGpsTrack(gpsTrack);
    }

    private void initCaches(ItemType trackedItem) {
        this.foilingTimeCache = createTimeRangeCache(trackedItem, "foilingTimeCache");
        this.foilingDistanceCache = createTimeRangeCache(trackedItem, "foilingDistanceCache");
        this.averageRideHeightCache = createTimeRangeCache(trackedItem, "averageRideHeightCache");
        this.expeditionAWACache = createTimeRangeCache(trackedItem, "expeditionAWACache");
        this.expeditionAWSCache= createTimeRangeCache(trackedItem, "expeditionAWSCache");
        this.expeditionTWACache= createTimeRangeCache(trackedItem, "expeditionTWACache");
        this.expeditionTWSCache= createTimeRangeCache(trackedItem, "expeditionTWSCache");
        this.expeditionTWDCache= createTimeRangeCache(trackedItem, "expeditionTWDCache");
        this.expeditionBoatSpeedCache= createTimeRangeCache(trackedItem, "expeditionBoatSpeedCache");
        this.expeditionTargBoatSpeedCache= createTimeRangeCache(trackedItem, "expeditionTargBoatSpeedCache");
        this.expeditionSOGCache= createTimeRangeCache(trackedItem, "expeditionSOGCache");
        this.expeditionCOGCache= createTimeRangeCache(trackedItem, "expeditionCOGCache");
        this.expeditionForestayLoadCache= createTimeRangeCache(trackedItem, "expeditionForestayLoadCache");
        this.expeditionRakeCache= createTimeRangeCache(trackedItem, "expeditionRakeCache");
        this.expeditionCourseDetailCache= createTimeRangeCache(trackedItem, "expeditionCourseDetailCache");
    }

    public GPSFixTrack<ItemType, GPSFixMoving> getGpsTrack() {
        return gpsTrack;
    }

    /**
     * Sets the {@link #gpsTrack} field and registers a cache invalidation listener
     * on the {@code gpsTrack} to ensure that the foiling distance is invalidated if needed.<p>
     * 
     * Must be called only once for a non-{@code null} {@code gpsTrack}.
     */
    protected void setGpsTrack(GPSFixTrack<ItemType, GPSFixMoving> gpsTrack) {
        if (gpsTrack != null) {
            assert this.gpsTrack == null;
        }
        this.gpsTrack = gpsTrack;
        if (gpsTrack != null) {
            gpsTrack.addListener(new CacheInvalidationGpsTrackListener());
        }
    }

    private <T> TimeRangeCache<T> createTimeRangeCache(ItemType trackedItem, final String cacheName) {
        return new TimeRangeCache<>(cacheName+" for "+trackedItem);
    }

    /**
     * After reading this object from an {@link ObjectInputStream}, initialize the caches properly.
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        initCaches(getTrackedItem());
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
            
            invalidateAllAtOrLaterThanForCaches(fixTimePoint, averageRideHeightCache, foilingDistanceCache,
                    foilingTimeCache, expeditionAWACache, expeditionAWSCache, expeditionTWACache, expeditionTWSCache,
                    expeditionTWDCache, expeditionBoatSpeedCache, expeditionTargBoatSpeedCache, expeditionSOGCache,
                    expeditionCOGCache, expeditionForestayLoadCache, expeditionRakeCache, expeditionCourseDetailCache);
        }
        return added;
    }
    
    private void invalidateAllAtOrLaterThanForCaches(TimePoint fixTimePoint, TimeRangeCache<?>... caches) {
        for (TimeRangeCache<?> timeRangeCache : caches) {
            timeRangeCache.invalidateAllAtOrLaterThan(fixTimePoint);
        }
    }

    interface ValueProvider{
        Double getValue(BravoExtendedFix fix);
    }
    
    @Override
    public Distance getAverageRideHeight(TimePoint from, TimePoint to) {
        final Pair<Distance, Long> nullElement = new Pair<>(Distance.NULL, 0l);
        Pair<Distance, Long> rideHeightSumAndCount = getValueSum(from, to, nullElement,
                (a, b)->new Pair<>(a.getA().add(b.getA()), a.getB()+b.getB()),
                averageRideHeightCache,
                /* valueCalculator */ new Track.TimeRangeValueCalculator<Pair<Distance, Long>>() {
            @Override
            public Pair<Distance, Long> calculate(TimePoint from, TimePoint to) {
                Distance sum = Distance.NULL;
                long count = 0;
                for (final BravoFix fix : getFixes(from, true, to, true)) {
                    final Distance rideHeight = fix.getRideHeight();
                    if (rideHeight != null) {
                        sum = sum.add(rideHeight);
                        count++;
                    }
                }
                return new Pair<>(sum, count);
            }
        });
        return rideHeightSumAndCount.getB() == 0l ? null : rideHeightSumAndCount.getA().scale(1./(double) rideHeightSumAndCount.getB());
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
        return getGpsTrack() == null ? null :
            getValueSum(from, to, /* nullElement */ Distance.NULL, Distance::add, foilingDistanceCache,
                /* valueCalculator */ new Track.TimeRangeValueCalculator<Distance>() {
            @Override
            public Distance calculate(TimePoint from, TimePoint to) {
                Distance result = Distance.NULL;
                TimePoint last = from;
                boolean isFoiling = false;
                for (final BravoFix fix : getFixes(from, true, to, true)) {
                    final boolean fixFoils = isFoiling(fix);
                    if (isFoiling && fixFoils) {
                        result = result.add(getGpsTrack().getDistanceTraveled(last, fix.getTimePoint()));
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
    
    @Override
    public Double getExpeditionAWAIfAvailable(TimePoint start, TimePoint endTimePoint){
        return getAverageOfBravoExtenededFixValueWithCachingForDouble(start, endTimePoint, BravoExtendedFix::getExpeditionAWA, expeditionAWACache);
    }

    @Override
    public Double getExpeditionAWSIfAvailable(TimePoint start, TimePoint endTimePoint){
        return getAverageOfBravoExtenededFixValueWithCachingForDouble(start, endTimePoint, BravoExtendedFix::getExpeditionAWS, expeditionAWSCache);
    }

    @Override
    public Double getExpeditionTWAIfAvailable(TimePoint start, TimePoint endTimePoint){
        return getAverageOfBravoExtenededFixValueWithCachingForDouble(start, endTimePoint, BravoExtendedFix::getExpeditionTWA, expeditionTWACache);
    }

    @Override
    public Double getExpeditionTWSIfAvailable(TimePoint start, TimePoint endTimePoint){
        return getAverageOfBravoExtenededFixValueWithCachingForDouble(start, endTimePoint, BravoExtendedFix::getExpeditionTWS, expeditionTWSCache);
    }

    @Override
    public Double getExpeditionTWDIfAvailable(TimePoint start, TimePoint endTimePoint){
        return getAverageOfBravoExtenededFixValueWithCachingForDouble(start, endTimePoint, BravoExtendedFix::getExpeditionTWD, expeditionTWDCache);
    }

    @Override
    public Double getExpeditionTargTWAIfAvailable(TimePoint start, TimePoint endTimePoint){
        return getAverageOfBravoExtenededFixValueWithCachingForDouble(start, endTimePoint, BravoExtendedFix::getExpeditionTWA, expeditionTWACache);
    }

    @Override
    public Double getExpeditionBoatSpeedIfAvailable(TimePoint start, TimePoint endTimePoint){
        return getAverageOfBravoExtenededFixValueWithCachingForDouble(start, endTimePoint, BravoExtendedFix::getExpeditionBSP, expeditionBoatSpeedCache);
    }

    @Override
    public Double getExpeditionTargBoatSpeedIfAvailable(TimePoint start, TimePoint endTimePoint){
        return getAverageOfBravoExtenededFixValueWithCachingForDouble(start, endTimePoint, BravoExtendedFix::getExpeditionBSP_TR, expeditionTargBoatSpeedCache);
    }

    @Override
    public Double getExpeditionSOGIfAvailable(TimePoint start, TimePoint endTimePoint){
        return getAverageOfBravoExtenededFixValueWithCachingForDouble(start, endTimePoint, BravoExtendedFix::getExpeditionSOG, expeditionSOGCache);
    }

    @Override
    public Double getExpeditionCOGIfAvailable(TimePoint start, TimePoint endTimePoint){
        return getAverageOfBravoExtenededFixValueWithCachingForDouble(start, endTimePoint, BravoExtendedFix::getExpeditionCOG, expeditionCOGCache);
    }

    @Override
    public Double getExpeditionForestayLoadIfAvailable(TimePoint start, TimePoint endTimePoint){
        return getAverageOfBravoExtenededFixValueWithCachingForDouble(start, endTimePoint, BravoExtendedFix::getExpeditionForestayLoad, expeditionForestayLoadCache);
    }

    @Override
    public Double getExpeditionRakeIfAvailable(TimePoint start, TimePoint endTimePoint){
        return getAverageOfBravoExtenededFixValueWithCachingForDouble(start, endTimePoint, BravoExtendedFix::getExpeditionRake, expeditionRakeCache);
    }

    @Override
    public Double getExpeditionCourseDetailIfAvailable(TimePoint start, TimePoint endTimePoint){
        return getAverageOfBravoExtenededFixValueWithCachingForDouble(start, endTimePoint, BravoExtendedFix::getExpeditionCourse, expeditionCourseDetailCache);
    }

    @Override
    public Double getExpeditionHeadingIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getExpeditionVMGIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getExpeditionVMGTargVMGDeltaIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getExpeditionRateOfTurnIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getExpeditionRudderAngleIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getExpeditionHeelIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getExpeditionTargetHeelIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getExpeditionTimeToPortLaylineIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getExpeditionTimeToStbLaylineIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getExpeditionDistToPortLaylineIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getExpeditionDistToStbLaylineIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Duration getExpeditionTimeToGUNIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getExpeditionTimeToCommitteeBoatIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getExpeditionTimeToPinIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Duration getExpeditionTimeToBurnToLineIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getExpeditionTimeToBurnToCommitteeBoatIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getExpeditionTimeToBurnToPinIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getExpeditionDistanceTocommitteeBoatIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getDistanceToPinDetailIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getDistanceBelowLineIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getLineSquareForWindIfAvailable(TimePoint start, TimePoint endTimePoint){
        // TODO Auto-generated method stub
        return null;
    }
    
    private Double getAverageOfBravoExtenededFixValueWithCachingForDouble(TimePoint from, TimePoint to,
            ValueProvider valueProvider, TimeRangeCache<Pair<Double, Long>> rangeCache) {
        final Pair<Double, Long> nullElement = new Pair<>(0.0, 0l);
        Pair<Double, Long> cacheValue = getValueSum(from, to, nullElement,
                (a, b) -> new Pair<>(a.getA() + b.getA(), a.getB() + b.getB()), rangeCache,
                /* valueCalculator */ new Track.TimeRangeValueCalculator<Pair<Double, Long>>() {
                    @Override
                    public Pair<Double, Long> calculate(TimePoint from, TimePoint to) {
                        Double sumValue = 0.0;
                        long count = 0;
                        for (final BravoFix fix : getFixes(from, true, to, true)) {
                            if (fix instanceof BravoExtendedFix) {
                                final Double value = valueProvider.getValue((BravoExtendedFix) fix);
                                if (value != null) {
                                    sumValue = sumValue + value;
                                    count++;
                                }
                            }
                        }
                        return new Pair<>(sumValue, count);
                    }
                });
        return cacheValue.getB() == 0l ? null : cacheValue.getA() * (1. / (double) cacheValue.getB());
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