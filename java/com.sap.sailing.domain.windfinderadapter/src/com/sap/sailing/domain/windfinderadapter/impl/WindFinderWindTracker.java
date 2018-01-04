package com.sap.sailing.domain.windfinderadapter.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.WindTracker;
import com.sap.sailing.domain.windfinderadapter.ReviewedSpotsCollection;
import com.sap.sailing.domain.windfinderadapter.Spot;
import com.sap.sse.common.Duration;
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
 * {@link WindFinderTrackerFactory} that this tracker has been stopped. The tracker will therefore be released by the
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
    private final WindFinderTrackerFactory factory;

    private ScheduledFuture<?> poller;
    
    public WindFinderWindTracker(DynamicTrackedRace trackedRace, WindFinderTrackerFactory factory) {
        this.trackedRace = trackedRace;
        this.factory = factory;
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
                final Wind wind = usefulSpot.getLatestMeasurement();
                if (wind != null) {
                    trackedRace.recordWind(wind, new WindSourceWithAdditionalID(WindSourceType.WINDFINDER, usefulSpot.getId()));
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception trying to obtain WindFinder data for race "+trackedRace.getRace().getName(), e);
        }
    }

    private Iterable<Spot> getUsefulSpots() throws MalformedURLException, IOException, ParseException {
        final Set<Spot> spots = new HashSet<>();
        for (final ReviewedSpotsCollection collection : factory.getReviewedSpotsCollections()) {
            // TODO bug1301 judge each spot's usefulness given the location of trackedRace
            Util.addAll(collection.getSpots(), spots);
        }
        return spots;
    }

    @Override
    public void stop() {
        poller.cancel(/* mayInterruptIfRunning */ false);
        factory.trackerStopped(trackedRace.getRace());
    }

}
