package com.sap.sailing.domain.windfinderadapter.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindTracker;
import com.sap.sailing.domain.windfinder.ReviewedSpotsCollection;
import com.sap.sailing.domain.windfinder.Spot;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.util.ThreadPoolUtil;

/**
 * When an object of this tracker type is created and there are WindFinder stations available, it schedules regular
 * requests to those WindFinder spots that are deemed useful for the race. If no location information for the race and
 * fleet is available yet, all sources available are considered useful. Otherwise, a rough distance check is performed.
 * If a measurement spot is ridiculously far away from the race course it doesn't make sense to even poll its
 * measurements, even if the general weighted average would ultimately rank down its contribution.
 * <p>
 * 
 * {@link #stop() Stopping} this tracker will cancel the task that polls for regular updates and will tell the
 * {@link WindFinderTrackerFactoryImpl} that this tracker has been stopped. The tracker will therefore be released by the
 * factory and will no longer be returned as the tracker responsible for retrieving WindFinder data for the particular
 * race that this tracker was bound to.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class WindFinderWindTracker implements WindTracker, Runnable {
    private static final Logger logger = Logger.getLogger(WindFinderWindTracker.class.getName());
    private static final Duration POLL_EVERY = Duration.ONE_MINUTE;
    
    private final DynamicTrackedRace trackedRace;
    private final WindFinderTrackerFactoryImpl factory;

    private final ScheduledFuture<?> poller;
    
    private WeakHashMap<Spot, TimePoint> timePointOfLastMeasurement;
    
    /**
     * The set of all {@link ReviewedSpotsCollection}s delivered by the {@link #factory} when this tracker
     * was created. This is the basis for {@link #getUsefulSpots()} when evaluating, e.g., based on the
     * {@link #trackedRace race's} location, which of the spots is actually useful for this tracker.
     */
    private final Iterable<ReviewedSpotsCollection> allSpotCollections;
    
    public WindFinderWindTracker(DynamicTrackedRace trackedRace, WindFinderTrackerFactoryImpl factory) throws InterruptedException, ExecutionException {
        this.trackedRace = trackedRace;
        this.factory = factory;
        this.allSpotCollections = factory.getReviewedSpotsCollections(/* cached */ false); // obtain fresh copy of all spots, updating cache
        this.timePointOfLastMeasurement = new WeakHashMap<>();
        this.poller = ThreadPoolUtil.INSTANCE.getDefaultBackgroundTaskThreadPoolExecutor().scheduleAtFixedRate(this,
                /* initialDelay */ 0, /* period */ POLL_EVERY.asMillis(), TimeUnit.MILLISECONDS);
    }
    
    /**
     * This method is executed in order to poll the WindFinder spots for new measurements
     */
    @Override
    public void run() {
        try {
            final Iterable<Spot> usefulSpots = getUsefulSpots();
            for (final Spot usefulSpot : usefulSpots) {
                final Iterable<Wind> windFixes = usefulSpot.getAllMeasurementsAfter(timePointOfLastMeasurement.get(usefulSpot));
                if (!Util.isEmpty(windFixes)) {
                    final WindSourceWithAdditionalID windSource = new WindSourceWithAdditionalID(WindSourceType.WINDFINDER, usefulSpot.getId());
                    final WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
                    for (final Wind wind : windFixes) {
                        final Wind existingFix = windTrack==null?null/*test scenario*/:windTrack.getFirstRawFixAtOrAfter(wind.getTimePoint());
                        if (existingFix == null || !existingFix.getTimePoint().equals(wind.getTimePoint())) {
                            // avoid duplicates by adding the same fix again 
                            trackedRace.recordWind(wind, windSource);
                            if (!timePointOfLastMeasurement.containsKey(usefulSpot) || wind.getTimePoint().after(timePointOfLastMeasurement.get(usefulSpot))) {
                                timePointOfLastMeasurement.put(usefulSpot, wind.getTimePoint());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception trying to obtain WindFinder data for race "+trackedRace.getRace().getName(), e);
        }
    }

    private Iterable<Spot> getUsefulSpots() throws MalformedURLException, IOException, ParseException, InterruptedException, ExecutionException {
        final Set<Spot> spots = new HashSet<>();
        for (final ReviewedSpotsCollection collection : allSpotCollections) {
            // TODO bug1301 judge each spot's usefulness given the location of trackedRace
            Util.addAll(collection.getSpots(/* cached */ false), spots);
        }
        return spots;
    }

    @Override
    public void stop() {
        poller.cancel(/* mayInterruptIfRunning */ false);
        factory.trackerStopped(trackedRace.getRace());
    }

}
