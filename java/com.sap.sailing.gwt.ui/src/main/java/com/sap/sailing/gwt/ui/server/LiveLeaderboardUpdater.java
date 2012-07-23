package com.sap.sailing.gwt.ui.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;

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
    
    private final String leaderboardName;
    
    private final SailingServiceImpl sailingService;
    
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
     * Column names for which {@link #currentLiveLeaderboard} has the details
     */
    private final Set<String> columnNamesForWhichCurrentLiveLeaderboardHasTheDetails;
    
    private boolean running;
    
    /**
     * For each String from <code>namesOfRaceColumnsForWhichToLoadLegDetails</code> passed to
     * {@link #getLiveLeaderboard(Collection)}, records the time of the last request here when the
     * {@link #currentLiveLeaderboard} has been updated with results containing this column details. This updater will
     * stop computing the details for that column if the time for the update calculation is more than
     * {@link #UPDATE_TIMEOUT_IN_MILLIS} milliseconds after the time point recorded here.<p>
     */
    private final Map<String, TimePoint> timePointOfLastRequestForColumnDetails;
    
    /**
     * As soon as the first {@link #getLiveLeaderboard(Collection)} request has been received, this field tells the
     * "validity time point" (as opposed to the request time point; so not when the request was received but for which
     * time point the request was asking the data) for which the last general request asked the leaderboard contents,
     * regardless the combination of column details requested. This is used to decide when to stop a thread running this
     * updater. See also {@link #run}.
     */ 
    private TimePoint lastRequest;
    
    private int cacheHitCount;
    private int cacheMissCount;
    
    public LiveLeaderboardUpdater(String leaderboardName, SailingServiceImpl sailingService) {
        this.leaderboardName = leaderboardName;
        this.sailingService = sailingService;
        this.timePointOfLastRequestForColumnDetails = new HashMap<String, TimePoint>();
        this.columnNamesForWhichCurrentLiveLeaderboardHasTheDetails = new HashSet<String>();
    }
    
    private Leaderboard getLeaderboard() {
        return sailingService.getService().getLeaderboardByName(leaderboardName);
    }
    
    public LeaderboardDTO getLiveLeaderboard(Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails) throws NoWindException {
        final MillisecondsTimePoint now = MillisecondsTimePoint.now();
        LeaderboardDTO result = null;
        synchronized (this) {
            if (columnNamesForWhichCurrentLiveLeaderboardHasTheDetails.containsAll(namesOfRaceColumnsForWhichToLoadLegDetails)) {
                result = currentLiveLeaderboard;
                cacheHitCount++;
            }
        }
        if (result == null) { // current cache doesn't have the column details requested; re-calculate
            cacheMissCount++;
            TimePoint timePoint = now.minus(getLeaderboard().getDelayToLiveInMillis());
            result = sailingService.computeLeaderboardByName(leaderboardName, timePoint, namesOfRaceColumnsForWhichToLoadLegDetails,
                    /* waitForLatestAnalyses */ false);
            updateRequestTimes(namesOfRaceColumnsForWhichToLoadLegDetails, result, timePoint);
            updateCacheContents(namesOfRaceColumnsForWhichToLoadLegDetails, result);
        }
        ensureRunning();
        logger.info(""+LiveLeaderboardUpdater.class.getSimpleName()+" cache hits/misses: "+cacheHitCount+"/"+cacheMissCount);
        return result;
    }
    
    private synchronized void ensureRunning() {
        if (!running) {
            start();
        }
    }

    private synchronized void start() {
        running = true;
        new Thread(this, "LiveLeaderboardUpdater for leaderboard "+getLeaderboard().getName()).start();
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
     */
    private synchronized void updateRequestTimes(Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails,
            LeaderboardDTO result, TimePoint timePoint) {
        lastRequest = timePoint;
        for (String nameOfRaceColumn : namesOfRaceColumnsForWhichToLoadLegDetails) {
            if (!timePointOfLastRequestForColumnDetails.containsKey(nameOfRaceColumn) ||
                    timePoint.after(timePointOfLastRequestForColumnDetails.get(nameOfRaceColumn))) {
                timePointOfLastRequestForColumnDetails.put(nameOfRaceColumn, timePoint);
            }
        }
    }

    private synchronized void updateCacheContents(Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails,
            LeaderboardDTO result) {
        columnNamesForWhichCurrentLiveLeaderboardHasTheDetails.clear();
        columnNamesForWhichCurrentLiveLeaderboardHasTheDetails.addAll(namesOfRaceColumnsForWhichToLoadLegDetails);
        currentLiveLeaderboard = result;
    }

    /**
     * Keeps computing the leaderboard for the column names that appear as key in {@link #timePointOfLastRequestForColumnDetails}
     * until no request has happened 
     */
    @Override
    public void run() {
        assert running;
        TimePoint timePoint = MillisecondsTimePoint.now().minus(getLeaderboard().getDelayToLiveInMillis());
        while (running) {
            try {
                final Set<String> namesOfRaceColumnsForWhichToLoadLegDetails = getColumnNamesForWhichToFetchDetails(timePoint);
                LeaderboardDTO newCacheValue = sailingService.computeLeaderboardByName(leaderboardName, timePoint,
                        namesOfRaceColumnsForWhichToLoadLegDetails, /* waitForLatestAnalyses */ false);
                updateCacheContents(namesOfRaceColumnsForWhichToLoadLegDetails, newCacheValue);
            } catch (NoWindException e) {
                logger.info("Unable to update cached leaderboard results for leaderboard "+leaderboardName+": "+e.getMessage());
                logger.throwing(LiveLeaderboardUpdater.class.getName(), "run", e);
                try {
                    Thread.sleep(1000); // avoid running into the same NoWindException too quickly
                } catch (InterruptedException e1) {
                    logger.throwing(LiveLeaderboardUpdater.class.getName(), "run", e1);
                }
            }
            timePoint = MillisecondsTimePoint.now().minus(getLeaderboard().getDelayToLiveInMillis());
            synchronized (this) {
                if (timePoint.asMillis() - lastRequest.asMillis() >= UPDATE_TIMEOUT_IN_MILLIS) {
                    running = false;
                    break; // make sure no-one sets running to true again while outside the synchronized block and before re-evaluating the while condition
                }
            }
        }
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
