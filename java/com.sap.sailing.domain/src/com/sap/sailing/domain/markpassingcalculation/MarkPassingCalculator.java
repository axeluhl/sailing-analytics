package com.sap.sailing.domain.markpassingcalculation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.markpassingcalculation.impl.CandidateChooserImpl;
import com.sap.sailing.domain.markpassingcalculation.impl.CandidateFinderImpl;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.util.impl.ThreadFactoryWithPriority;

/**
 * Calculates the {@link MarkPassing}s for a {@link DynamicTrackedRace} using an {@link CandidateFinder} and an
 * {@link CandidateChooser}. The finder evaluates the fixes and finds possible MarkPassings as {@link Candidate}s . The
 * chooser than finds the most likely sequence of {@link Candidate}s and updates the race with new {@link MarkPassing}s
 * for this sequence. Upon calling the constructor {@link #MarkPassingCalculator(DynamicTrackedRace, boolean)} this
 * happens for the current state of the race. In addition, for live races, the <code>listen</code> parameter of the
 * constructor should be true. Then a {@link MarkPassingUpdateListener} is initialized which puts new fixes into a queue
 * as {@link StorePositionUpdateStrategy}. A new thread will also be started to evaluate the new fixes (See
 * {@link CandidateFinder} and {@link CandidateChooser}). This continues until the {@link MarkPassingUpdateListener}
 * signals that the race is over (after {@link #stop()} is called.
 * 
 * @author Nicolas Klose
 * 
 */
public class MarkPassingCalculator {
    private final DynamicTrackedRace race;
    private CandidateFinder finder;
    private CandidateChooser chooser;
    private static final Logger logger = Logger.getLogger(MarkPassingCalculator.class.getName());
    private final MarkPassingUpdateListener listener;
    private final static ExecutorService executor = new ThreadPoolExecutor(/* corePoolSize */Math.max(Runtime
            .getRuntime().availableProcessors() - 1, 3),
    /* maximumPoolSize */Math.max(Runtime.getRuntime().availableProcessors() - 1, 3),
    /* keepAliveTime */60, TimeUnit.SECONDS,
    /* workQueue */new LinkedBlockingQueue<Runnable>(), new ThreadFactoryWithPriority(Thread.NORM_PRIORITY - 1));

    private boolean suspended = false;

    public MarkPassingCalculator(DynamicTrackedRace race, boolean listen) {
        if (listen) {
            listener = new MarkPassingUpdateListener(race);
        } else {
            listener = null;
        }
        this.race = race;
        finder = new CandidateFinderImpl(race);
        chooser = new CandidateChooserImpl(race);
        for (Competitor c : race.getRace().getCompetitors()) {
            Pair<Iterable<Candidate>, Iterable<Candidate>> allCandidates = finder.getAllCandidates(c);
            chooser.calculateMarkPassDeltas(c, allCandidates.getA(), allCandidates.getB());
        }
        if (listen) {
            new Thread(new Listen(), "MarkPassingCalculator listener for race " + race.getRace().getName()).start();
        }
    }

    /**
     * Waits until an object is in the queue, then drains it entirely. After that, the fixes are sorted by the object
     * they are tracking in <code>competitorFixes</code> and <code>markFixes</code>. Finally, if <code>suspended</code>
     * is false, new passing are computed.
     * 
     * @author Nicolas Klose
     * 
     */
    private class Listen implements Runnable {
        @Override
        public void run() {
            logger.fine("MarkPassingCalculator is listening for new Fixes.");
            boolean finished = false;
            Map<Competitor, List<GPSFix>> competitorFixes = new HashMap<>();
            Map<Mark, List<GPSFix>> markFixes = new HashMap<>();
            List<Waypoint> addedWaypoints = new ArrayList<>();
            List<Waypoint> removedWaypoints = new ArrayList<>();
            Integer smallestChangedWaypointIndex = null;
            List<MarkPassing> fixedMarkPassings = new ArrayList<>();
            List<MarkPassing> removedMarkPassings = new ArrayList<>();
            while (!finished) {
                List<StorePositionUpdateStrategy> allNewFixInsertions = new ArrayList<>();
                try {
                    allNewFixInsertions.add(listener.getQueue().take());
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "MarkPassingCalculator threw exception " + e.getMessage()
                            + " while waiting for new GPSFixes");
                }
                listener.getQueue().drainTo(allNewFixInsertions);
                for (StorePositionUpdateStrategy fixInsertion : allNewFixInsertions) {
                    if (listener.isEndMarker(fixInsertion)) {
                        finished = true;
                    } else {
                        fixInsertion.storePositionUpdate(competitorFixes, markFixes, addedWaypoints, removedWaypoints,
                                smallestChangedWaypointIndex, fixedMarkPassings, removedMarkPassings);
                    }
                }
                if (!suspended) {
                    // TODO Interplay between changing waypoints and setting fixed passes?
                    if (smallestChangedWaypointIndex != null) {
                        Map<Competitor, Pair<List<Candidate>, List<Candidate>>> candidateDeltas = finder
                                .updateWaypoints(addedWaypoints, removedWaypoints, smallestChangedWaypointIndex);
                        chooser.removeWaypoints(removedWaypoints);
                        for (Entry<Competitor, Pair<List<Candidate>, List<Candidate>>> entry : candidateDeltas
                                .entrySet()) {
                            Pair<List<Candidate>, List<Candidate>> pair = entry.getValue();
                            chooser.calculateMarkPassDeltas(entry.getKey(), pair.getA(), pair.getB());
                        }
                    }
                    updateFixedMarkPassings(fixedMarkPassings, removedMarkPassings);
                    computeMarkPasses(competitorFixes, markFixes);
                    competitorFixes.clear();
                    markFixes.clear();
                }
            }
        }

        private void updateFixedMarkPassings(List<MarkPassing> fixedMarkPassings, List<MarkPassing> removedMarkPassings) {
            for (MarkPassing m : removedMarkPassings) {
                chooser.removeFixedPassing(m.getCompetitor(), m.getWaypoint());
            }
            for (MarkPassing m : fixedMarkPassings) {
                chooser.setFixedPassing(m.getCompetitor(), m.getWaypoint(), m.getTimePoint());
            }
        }

        /**
         * The calculation has two steps. For every mark with new fixes those competitor fixes are calculated that may
         * have changed their status as {@link Candidate} (see {@link FixesAffectedByNewMarkFixes}. Then, the
         * {@link CandidateFinder} uses those fixes along with any new competitor fixes to calculate any new or wrong
         * Candidates. These are passed to the {@link CandidateChooser} to calculate any new {@link MarkPassing}s (see
         * {@link ComputeMarkPassings}).
         * 
         */
        private void computeMarkPasses(Map<Competitor, List<GPSFix>> newCompetitorFixes,
                Map<Mark, List<GPSFix>> newMarkFixes) {
            Map<Competitor, Set<GPSFix>> combinedCompetitorFixesFixes = new HashMap<>();
            List<FutureTask<Map<Competitor, List<GPSFix>>>> markTasks = new ArrayList<>();
            for (Entry<Mark, List<GPSFix>> markEntry : newMarkFixes.entrySet()) {
                FutureTask<Map<Competitor, List<GPSFix>>> task = new FutureTask<>(new FixesAffectedByNewMarkFixes(
                        markEntry.getKey(), markEntry.getValue()));
                markTasks.add(task);
                executor.submit(task);
            }
            newMarkFixes.clear();
            for (Entry<Competitor, List<GPSFix>> competitorEntry : newCompetitorFixes.entrySet()) {
                Set<GPSFix> fixesForCompetitor = combinedCompetitorFixesFixes.get(competitorEntry.getKey());
                if (newCompetitorFixes == null) {
                    fixesForCompetitor = new HashSet<>();
                    combinedCompetitorFixesFixes.put(competitorEntry.getKey(), fixesForCompetitor);
                }
                fixesForCompetitor.addAll(competitorEntry.getValue());
            }
            for (FutureTask<Map<Competitor, List<GPSFix>>> task : markTasks) {
                Map<Competitor, List<GPSFix>> fixes = new HashMap<>();
                try {
                    fixes = task.get();
                } catch (InterruptedException | ExecutionException e) {
                    logger.severe("Threw Exception " + e.getMessage()
                            + " while calculating fixes affected by new Mark Fixes.");
                }
                for (Entry<Competitor, List<GPSFix>> competitorEntry : fixes.entrySet()) {
                    Set<GPSFix> fixesForCompetitor = combinedCompetitorFixesFixes.get(competitorEntry.getKey());
                    if (newCompetitorFixes == null) {
                        fixesForCompetitor = new HashSet<>();
                        combinedCompetitorFixesFixes.put(competitorEntry.getKey(), fixesForCompetitor);
                    }
                    fixesForCompetitor.addAll(competitorEntry.getValue());
                }
            }
            newCompetitorFixes.clear();
            List<Callable<Object>> tasks = new ArrayList<>();
            for (Entry<Competitor, Set<GPSFix>> c : combinedCompetitorFixesFixes.entrySet()) {
                tasks.add(Executors.callable(new ComputeMarkPassings(c.getKey(), c.getValue())));
            }
            try {
                executor.invokeAll(tasks);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private class ComputeMarkPassings implements Runnable {
            Competitor c;
            Iterable<GPSFix> fixes;

            public ComputeMarkPassings(Competitor c, Iterable<GPSFix> fixes) {
                this.c = c;
                this.fixes = fixes;
            }

            @Override
            public void run() {
                logger.finest("Calculating MarkPassings for " + c);
                Pair<Iterable<Candidate>, Iterable<Candidate>> candidateDeltas = finder.getCandidateDeltas(c, fixes);
                chooser.calculateMarkPassDeltas(c, candidateDeltas.getA(), candidateDeltas.getB());
            }
        }

        private class FixesAffectedByNewMarkFixes implements Callable<Map<Competitor, List<GPSFix>>> {
            Mark m;
            Iterable<GPSFix> fixes;

            public FixesAffectedByNewMarkFixes(Mark m, Iterable<GPSFix> fixes) {
                this.m = m;
                this.fixes = fixes;
            }

            @Override
            public Map<Competitor, List<GPSFix>> call() {
                return finder.calculateFixesAffectedByNewMarkFixes(m, fixes);
            }
        }
    }

    /**
     * Only suspends the actual calculation. Even when suspended all incoming fixes are added to the queue and sorted.
     */
    public void suspend() {
        suspended = true;
    }

    /**
     * An empty object is written to the queue to ensure that any fixes that have been removed from the queue are
     * evaluated even if nothing else arrives after this is called.
     */
    public void resume() {
        suspended = false;
        listener.getQueue().add(new StorePositionUpdateStrategy() {
            @Override
            public void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes,
                    Map<Mark, List<GPSFix>> markFixes, List<Waypoint> addedWaypoints, List<Waypoint> removedWaypoints,
                    Integer smallestChangedWaypointIndex, List<MarkPassing> fixMarkPassing,
                    List<MarkPassing> removeFixedMarkPassing) {
            }
        });
    }

    public void stop() {
        listener.stop();
    }

    public void recalculateEverything() {
        finder = new CandidateFinderImpl(race);
        chooser = new CandidateChooserImpl(race);
        for (Competitor c : race.getRace().getCompetitors()) {
            Pair<Iterable<Candidate>, Iterable<Candidate>> allCandidates = finder.getAllCandidates(c);
            chooser.calculateMarkPassDeltas(c, allCandidates.getA(), allCandidates.getB());
        }
    }

    public void addFixedPassing(MarkPassing m) {
        listener.addFixedPassing(m);
    }

    public void removeFixedPassing(MarkPassing m) {
        listener.removeFixedPassing(m);
    }
}