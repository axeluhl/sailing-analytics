package com.sap.sailing.domain.leaderboard.caching;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.sharding.ShardingType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.sharding.ShardingContext;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Computing a leaderboard live, particularly when the fleet tracked is large, requires considerable resources.
 * Live tracking events such as new GPS fixes and wind data make caching hard because caches need to be invalidated
 * frequently. Instead of computing a {@link LeaderboardDTO} on the fly when a client asks for it, in live mode it
 * is more adequate to keep updating a ("whiteboard") cache with results which can be served to a client in live mode.
 * <p>
 * 
 * This updater, when run, keeps updating its {@link #getLiveLeaderboard live leaderboard}. It is specific to a single
 * leaderboard name and accumulates a set of race columns for which to obtain the details. It stops when no client has been asking
 * for the live leaderboard's contents for more then {@link #UPDATE_TIMEOUT_IN_MILLIS} milliseconds. It is automatically re-started
 * by the next request received.<p>
 * 
 * Clients pass their requested set of expanded columns on to this updater when they fetch the current live result from this
 * cache. Each column requested by a client will continue to be updated by this updater into the live leaderboard result
 * for a {@link #UPDATE_TIMEOUT_IN_MILLIS specific time}.<p>
 * 
 * The updater keeps computing updates until {@link #UPDATE_TIMEOUT_IN_MILLIS} after the last client request for the
 * leaderboard for which this updater is responsible has been received. Then it terminates in a "synchronized way." It can
 * be re-activated. Re-activating it works using the same synchronization facility so that no two threads for this updater
 * are running at the same time.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class LiveLeaderboardUpdater implements Runnable {
    private static final Logger logger = Logger.getLogger(LiveLeaderboardUpdater.class.getName());
    
    /**
     * For how long shall the updater keep pre-computing results after the last request has been received; currently 60s
     */
    private static final long UPDATE_TIMEOUT_IN_MILLIS = 60000l;
    
    /**
     * A kind of "throttle"; if leaderboard updates are computed in less than this time, don't start a new update before this time has
     * passed since starting the last update.
     */
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000l;
    
    private final Leaderboard leaderboard;
    
    /**
     * Live leaderboard results containing column details for all column names in
     * {@link #columnNamesForWhichCurrentLiveLeaderboardHasTheDetails}. May be <code>null</code> if
     * {@link #columnNamesForWhichCurrentLiveLeaderboardHasTheDetails} is empty in case no leaderboard with no column
     * details has been computed yet. Vice versa, may contain a non-<code>null</code> result even if
     * {@link #columnNamesForWhichCurrentLiveLeaderboardHasTheDetails} is empty because clients may only be interested
     * currently in a live leaderboard with no column details.
     */
    private LeaderboardDTO currentLiveLeaderboard;
    
    /**
     * When the evaluation of the live leaderboard has resulted in an exception, the exception will be passed on
     * to callers of {@link #getLiveLeaderboard(Collection, boolean)}, wrapped by an {@link ExecutionException}.
     * 
     */
    private Exception currentException;
    
    /**
     * Column names for which {@link #currentLiveLeaderboard} has the details
     */
    private final Set<String> columnNamesForWhichCurrentLiveLeaderboardHasTheDetails;
    
    private boolean currentLiveLeaderboardHasOverallDetails;
    
    private boolean running;
    
    /**
     * For each String from <code>namesOfRaceColumnsForWhichToLoadLegDetails</code> passed to
     * {@link #getLiveLeaderboard(Collection, boolean)}, records the validity time point of the last request here when the
     * {@link #currentLiveLeaderboard} has been updated with results containing this column details. This updater will
     * stop computing the details for that column if the validity time for the update calculation is more than
     * {@link #UPDATE_TIMEOUT_IN_MILLIS} milliseconds after the time point recorded here.
     * <p>
     */
    private final Map<String, TimePoint> timePointOfLastRequestForColumnDetails;
    
    /**
     * Tells when {@link #updateRequestTimes(Collection, boolean)} was last called with the request for overall details
     */
    private TimePoint timePointOfLastRequestForOverallDetails;
    
    /**
     * As soon as the first {@link #getLiveLeaderboard(Collection, boolean)} request has been received, this field tells the
     * "validity time point" (as opposed to the request time point; so not when the request was received but for which
     * time point the request was asking the data) for which the last general request asked the leaderboard contents,
     * regardless the combination of column details requested. This is used to decide when to stop a thread running this
     * updater. See also {@link #run}.
     */ 
    private TimePoint lastRequest;
    
    private int cacheHitCount;
    private int cacheMissCount;

    /**
     * If {@link #running}, holds the thread created by {@link #start()}. This thread will be stopped if computing an update
     * takes longer than {@link #UPDATE_TIMEOUT_IN_MILLIS} milliseconds.
     */
    private Thread thread;

    private TrackedRegattaRegistry trackedRegattaRegistry;

    private DomainFactory baseDomainFactory;
    
    public LiveLeaderboardUpdater(Leaderboard leaderboard, TrackedRegattaRegistry trackedRegattaRegistry, DomainFactory baseDomainFactory) {
        this.leaderboard = leaderboard;
        this.trackedRegattaRegistry = trackedRegattaRegistry;
        this.baseDomainFactory = baseDomainFactory;
        this.timePointOfLastRequestForColumnDetails = new HashMap<String, TimePoint>();
        this.columnNamesForWhichCurrentLiveLeaderboardHasTheDetails = new HashSet<String>();
    }
    
    private Leaderboard getLeaderboard() {
        return leaderboard;
    }
    
    /**
     * If the calculation of the live leaderboard has terminated abnormally by throwing an exception during the last
     * re-calculation attempt, another re-calculation is waited for. If that again terminates abnormally by throwing an
     * exception, that exception will be thrown by this method, wrapped by an {@link ExecutionException}. Otherwise,
     * this method will return the last live leaderboard calculated that has the columns and details requested. If the
     * last live leaderboard calculated doesn't have the requested properties, the method waits for the next calculation
     * to finish which is expected to provide the leaderboard for the details and columns requested.
     */
    public LeaderboardDTO getLiveLeaderboard(Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails, boolean addOverallDetails) throws NoWindException, ExecutionException {
        LeaderboardDTO result = null;
        updateRequestTimes(namesOfRaceColumnsForWhichToLoadLegDetails, addOverallDetails);
        ensureRunning();
        synchronized (this) {
            if (running
                    && columnNamesForWhichCurrentLiveLeaderboardHasTheDetails
                            .containsAll(namesOfRaceColumnsForWhichToLoadLegDetails)
                            && (!addOverallDetails || currentLiveLeaderboardHasOverallDetails)) {
                result = currentLiveLeaderboard;
                if (result != null) {
                    cacheHitCount++;
                }
            }
        }
        if (result == null) { // current cache doesn't have the column details requested; "request" has been entered by
            // updating the request times; now ensure the thread is running, then wait for the next result;
            // The biggest challenge occurs when the thread is currently working on an update and takes longer than
            // the UPDATE_TIMEOUT_IN_MILLIS for it. Then, all previous requests would be so far in the past that the
            // thread is terminated and that in addition to that the original request would meanwhile have expired.
            // We then need to renew the request.
            cacheMissCount++;
            synchronized (this) {
                while (result == null) {
                    if (columnNamesForWhichCurrentLiveLeaderboardHasTheDetails.containsAll(namesOfRaceColumnsForWhichToLoadLegDetails) &&
                            (!addOverallDetails || currentLiveLeaderboardHasOverallDetails)) {
                        result = currentLiveLeaderboard;
                    }
                    if (result == null) {
                        logger.finest(()->"waiting for leaderboard for "+namesOfRaceColumnsForWhichToLoadLegDetails+" and addOverallDetails="+addOverallDetails);
                        ensureRunning();
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            logger.log(Level.INFO, "interrupted while waiting for LiveLeaderboardCache update", e);
                        }
                        if (currentException != null) {
                            throw new ExecutionException(currentException);
                        }
                        if (columnNamesForWhichCurrentLiveLeaderboardHasTheDetails.containsAll(namesOfRaceColumnsForWhichToLoadLegDetails) &&
                                (!addOverallDetails || currentLiveLeaderboardHasOverallDetails)) {
                            result = currentLiveLeaderboard;
                            logger.finest(()->"successfully waited for leaderboard for "+namesOfRaceColumnsForWhichToLoadLegDetails+" and addOverallDetails="+addOverallDetails);
                        } else {
                            logger.finest(()->"waiting for leaderboard for "+namesOfRaceColumnsForWhichToLoadLegDetails+" and addOverallDetails="+addOverallDetails+" unsuccessful. Need to try again...");
                        }
                    } else {
                        logger.finest(()->"leaderboard for "+namesOfRaceColumnsForWhichToLoadLegDetails+" and addOverallDetails="+addOverallDetails+" was provided in the meantime");
                    }
                    // now we either have a result (good, we're done), or the thread stopped running (then we need to renew the request)
                    // or the thread still runs but the result may have expired and therefore be null (renew the request)
                    if (result == null) {
                        // need to renew the request
                        updateRequestTimes(namesOfRaceColumnsForWhichToLoadLegDetails, addOverallDetails);
                        ensureRunning();
                    }
                }
            }
        }
        logger.info(""+LiveLeaderboardUpdater.class.getSimpleName()+" for "+getLeaderboard().getName()+
                " cache hits/misses: "+cacheHitCount+"/"+cacheMissCount);
        return result;
    }

    private synchronized void ensureRunning() {
        if (!running) {
            start();
        }
    }

    private synchronized void start() {
        running = true;
        try {
            thread = new Thread(this, "LiveLeaderboardUpdater for leaderboard "+getLeaderboard().getName());
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e) {
            running = false;
            logger.log(Level.SEVERE, "Error creating LiveLeaderboardUpdater thread for leadedrboard "+getLeaderboard().getName(), e);
            throw e;
        }
    }

    /**
     * Updates {@link #lastRequest} with <code>timePoint</code> and the {@link #timePointOfLastRequestForColumnDetails
     * latest request times} for each of the column names in <code>namesOfRaceColumnsForWhichToLoadLegDetails</code> to
     * <code>timePoint</code> if <code>timePoint</code> is newer than the last request time for the column details so
     * far. Stores <code>result</code> as the {@link #currentLiveLeaderboard cache value} if
     * <code>namesOfRaceColumnsForWhichToLoadLegDetails</code> contains all column names already in
     * {@link #timePointOfLastRequestForColumnDetails}'s key set.
     * <p>
     * 
     * This method assumes that <code>namesOfRaceColumnsForWhichToLoadLegDetails</code> tells the column names for which
     * <code>result</code> has the details.
     * 
     * @param addOverallDetails tells whether overall details are requested for the resulting leaderboard; see also
     * {@link Leaderboard#computeDTO(TimePoint, Collection, boolean, boolean, TrackedRegattaRegistry, DomainFactory, boolean)}.
     */
    private synchronized void updateRequestTimes(Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails, boolean addOverallDetails) {
        lastRequest = getLeaderboard().getNowMinusDelay();
        for (String nameOfRaceColumn : namesOfRaceColumnsForWhichToLoadLegDetails) {
            if (!timePointOfLastRequestForColumnDetails.containsKey(nameOfRaceColumn) ||
                    lastRequest.after(timePointOfLastRequestForColumnDetails.get(nameOfRaceColumn))) {
                timePointOfLastRequestForColumnDetails.put(nameOfRaceColumn, lastRequest);
            }
        }
        if (addOverallDetails && (timePointOfLastRequestForOverallDetails == null || lastRequest.after(timePointOfLastRequestForOverallDetails))) {
            timePointOfLastRequestForOverallDetails = lastRequest;
        }
    }

    /**
     * Updates the cache contents and notifies all waiters on this object.
     */
    private synchronized void updateCacheContents(Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails,
            boolean addOverallDetails, LeaderboardDTO result) {
        currentException = null;
        columnNamesForWhichCurrentLiveLeaderboardHasTheDetails.clear();
        columnNamesForWhichCurrentLiveLeaderboardHasTheDetails.addAll(namesOfRaceColumnsForWhichToLoadLegDetails);
        currentLiveLeaderboard = result;
        currentLiveLeaderboardHasOverallDetails = addOverallDetails;
        notifyAll();
    }

    /**
     * Keeps computing the leaderboard for the column names that appear as key in
     * {@link #timePointOfLastRequestForColumnDetails} and for the overall details as noted in
     * {@link #timePointOfLastRequestForOverallDetails} until no request has happened during the
     * {@link #UPDATE_TIMEOUT_IN_MILLIS timeout period}.
     */
    @Override
    public void run() {
        assert running;
        try {
            ShardingContext.setShardingConstraint(ShardingType.LEADERBOARDNAME, leaderboard.getName());
            logger.info("Starting " + LiveLeaderboardUpdater.class.getSimpleName() + " thread for leaderboard "
                    + leaderboard.getName());
            // interrupt the current thread if not producing a single result within the overall timeout
            while (true) {
                TimePoint now = MillisecondsTimePoint.now();
                final Long delayToLiveInMillis = getLeaderboard().getDelayToLiveInMillis();
                TimePoint timePoint = delayToLiveInMillis == null ? now : now.minus(delayToLiveInMillis);
                synchronized (this) {
                    if (timePoint.asMillis() - lastRequest.asMillis() >= UPDATE_TIMEOUT_IN_MILLIS) {
                        running = false;
                        currentLiveLeaderboard = null; // declare cache contents for invalid because they will later be too old
                        break; // make sure no-one sets running to true again while outside the synchronized block and
                               // before re-evaluating the while condition
                    }
                }
                TimePoint timeLastUpdateWasStarted = now;
                try {
                    final Set<String> namesOfRaceColumnsForWhichToLoadLegDetails = getColumnNamesForWhichToFetchDetails(timePoint);
                    final boolean addOverallDetails = getOverallDetails(timePoint);
                    LeaderboardDTO newCacheValue = leaderboard.computeDTO(timePoint,
                            namesOfRaceColumnsForWhichToLoadLegDetails, addOverallDetails,
                            /* waitForLatestAnalyses */false, trackedRegattaRegistry, baseDomainFactory, /* fillTotalPointsUncorrected */ false);
                    updateCacheContents(namesOfRaceColumnsForWhichToLoadLegDetails, addOverallDetails, newCacheValue);
                } catch (NoWindException e) {
                    logger.log(Level.SEVERE, "Exception during re-calculating the live leaderboard "+leaderboard.getName(), e);
                    try {
                        Thread.sleep(1000); // avoid running into the same NoWindException too quickly
                    } catch (InterruptedException e1) {
                        logger.throwing(LiveLeaderboardUpdater.class.getName(), "run", e1);
                    }
                }
                TimePoint computeLeaderboardByNameFinishedAt = MillisecondsTimePoint.now();
                long millisToSleep = MINIMUM_TIME_BETWEEN_UPDATES
                        - (computeLeaderboardByNameFinishedAt.asMillis() - timeLastUpdateWasStarted.asMillis());
                if (millisToSleep > 0) {
                    try {
                        Thread.sleep(millisToSleep);
                    } catch (InterruptedException e) {
                        logger.throwing(LiveLeaderboardUpdater.class.getName(), "run", e);
                    }
                }
            }
            logger.info("" + LiveLeaderboardUpdater.class.getSimpleName() + " thread for leaderboard "
                    + leaderboard.getName() + " ending");
        } catch (Exception e) {
            synchronized (this) {
                running = false;
                currentLiveLeaderboard = null;
                currentException = e;
                notifyAll();
            }
            logger.log(Level.SEVERE, "exception updating live leaderboard "+leaderboard.getName(), e);
        } finally {
            ShardingContext.clearShardingConstraint(ShardingType.LEADERBOARDNAME);
        }
    }

    /**
     * Determines based on {@link #timePointOfLastRequestForOverallDetails} and the <code>timePoint</code> parameter
     * if the duration between then is less than {@link #UPDATE_TIMEOUT_IN_MILLIS} in which case overall details are to
     * be loaded. If {@link #timePointOfLastRequestForOverallDetails} is <code>null</code>, meaning that overall details
     * have never been requested for the leaderboard before, <code>false</code> is returned immediately.
     */
    private boolean getOverallDetails(TimePoint timePoint) {
        return timePointOfLastRequestForOverallDetails != null &&
                !timePoint.after(timePointOfLastRequestForOverallDetails.plus(UPDATE_TIMEOUT_IN_MILLIS));
    }
    
    /**
     * Determines those column names from {@link #timePointOfLastRequestForColumnDetails}'s keys for which the last
     * request is less then {@link #UPDATE_TIMEOUT_IN_MILLIS} milliseconds before <code>timePoint</code>
     */
    private Set<String> getColumnNamesForWhichToFetchDetails(TimePoint timePoint) {
        Set<String> result = new HashSet<String>();
        TimePoint expiredIfBefore = timePoint.minus(UPDATE_TIMEOUT_IN_MILLIS);
        for (Map.Entry<String, TimePoint> e : timePointOfLastRequestForColumnDetails.entrySet()) {
            if (e.getValue().after(expiredIfBefore)) {
                result.add(e.getKey());
            }
        }
        return result;
    }
}
