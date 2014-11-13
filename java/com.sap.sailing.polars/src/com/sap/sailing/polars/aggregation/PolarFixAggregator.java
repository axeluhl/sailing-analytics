package com.sap.sailing.polars.aggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.data.PolarFix;
import com.sap.sse.common.Util.Pair;

/**
 * Allows extracting data about average speed for each angle creating the foundation of polar-sheet generation. Tasks
 * are assigned to the executor per race & per competitor and the results are filled on the go to allow progress
 * indication in the polar-sheet itself.
 * 
 * @author D054528 Frederik Petersen
 * 
 */
public class PolarFixAggregator implements Future<Map<RegattaAndRaceIdentifier, List<PolarFix>>> {

    private final Set<PolarFixAggregationWorker> workers;

    private final Executor executor;

    private final Map<RegattaAndRaceIdentifier, List<PolarFix>> fixes = new HashMap<RegattaAndRaceIdentifier, List<PolarFix>>();

    /**
     * Will prepare the {@link PolarFixAggregationWorker}s per race & per competitor. This includes determining start
     * and end time.
     * 
     * @param updateInterval
     *            from which the data is to be collected
     * @param settings
     * @param executor
     *            executes the tasks upon {@link #startPolarFixAggregation()}
     */
    public PolarFixAggregator(PolarFixRaceInterval updateInterval, PolarSheetGenerationSettings settings,
            Executor executor) {
        this.executor = executor;
        workers = new HashSet<PolarFixAggregationWorker>();
        for (Entry<TrackedRace, Map<Competitor, Pair<TimePoint, TimePoint>>> entry : updateInterval
                .getCompetitorAndTimepointsForRace().entrySet()) {
            TrackedRace race = entry.getKey();
            TimePoint startTime = race.getStartOfRace();
            TimePoint endTime = race.getEndOfRace();
            if (endTime == null) {
                endTime = race.getTimePointOfNewestEvent();
            }
            RaceDefinition raceDefinition = race.getRace();

            Map<Competitor, Pair<TimePoint, TimePoint>> detailedInterval = entry.getValue();
            if (detailedInterval == null) {
                Iterable<Competitor> competitors = raceDefinition.getCompetitors();
                for (Competitor competitor : competitors) {
                    PolarFixAggregationWorker task = new PolarFixAggregationWorker(race, this, startTime, endTime,
                            competitor, settings, null);
                    workers.add(task);
                }
            } else {
                for (Entry<Competitor, Pair<TimePoint, TimePoint>> competitorEntry : detailedInterval.entrySet()) {
                    Competitor competitor = competitorEntry.getKey();
                    Pair<TimePoint, TimePoint> intervalBeginningAndEnd = competitorEntry.getValue();
                    PolarFixAggregationWorker task = new PolarFixAggregationWorker(race, this, startTime, endTime,
                            competitor, settings, intervalBeginningAndEnd);
                    workers.add(task);
                }
            }

        }
    }

    /**
     * Starts the {@link PolarFixAggregationWorker}s
     */
    public void startPolarFixAggregation() {
        for (Runnable task : workers) {
            executor.execute(task);
        }
    }

    /**
     * To be called from {@link PolarFixAggregationWorker} for adding a datapoint to the list of
     * results
     *            wind's speed in knots
     */
    protected void addPolarFix(RegattaAndRaceIdentifier raceId, PolarFix polarFix) {
        synchronized (fixes) {
            List<PolarFix> existingList = fixes.get(raceId);
            if (existingList == null) {
                existingList = new ArrayList<PolarFix>();
                fixes.put(raceId, existingList);
            }
            existingList.add(polarFix);
        }
    }

    private boolean allWorkersDone() {
        boolean complete = true;
        for (PolarFixAggregationWorker task : workers) {
            if (!task.isDone()) {
                complete = false;
                break;
            }
        }
        return complete;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return allWorkersDone();
    }

    @Override
    public Map<RegattaAndRaceIdentifier, List<PolarFix>> get() throws InterruptedException, ExecutionException {
        while (!isDone()) {
            Thread.sleep(100);
        }
        return fixes;
    }

    @Override
    public Map<RegattaAndRaceIdentifier, List<PolarFix>> get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException,
            TimeoutException {
        long timeRun = 0;
        long timeoutInMillis = unit.toMillis(timeout);
        while (!isDone() && timeRun < timeoutInMillis) {
            Thread.sleep(100);
            timeRun = timeRun + 100;
        }
        if (timeRun >= timeoutInMillis) {
            throw new TimeoutException();
        }
        return fixes;
    }

    public Set<PolarFix> getAggregationResultAsSingleList() throws InterruptedException,
            ExecutionException {
        Set<PolarFix> fixes = new HashSet<PolarFix>();
        Map<RegattaAndRaceIdentifier, List<PolarFix>> aggregationResult = get();
        for (List<PolarFix> list : aggregationResult.values()) {
            fixes.addAll(list);
        }
        return fixes;
    }

}
