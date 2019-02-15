package com.sap.sailing.domain.markpassingcalculation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.markpassingcalculation.impl.CandidateChooserImpl;
import com.sap.sailing.domain.markpassingcalculation.impl.CandidateFinderImpl;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.util.IntHolder;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;
import com.sap.sse.util.ThreadPoolUtil;

/**
 * Calculates the {@link MarkPassing}s for a {@link DynamicTrackedRace} using an {@link CandidateFinder} and an
 * {@link CandidateChooser}. The finder evaluates the fixes and finds possible MarkPassings as {@link Candidate}s . The
 * chooser than finds the most likely sequence of {@link Candidate}s and updates the race with new {@link MarkPassing}s
 * for this sequence. Upon calling the constructor {@link #MarkPassingCalculator(DynamicTrackedRace, boolean, boolean)} this
 * happens for the current state of the race. In addition, for live races, the <code>listen</code> parameter of the
 * constructor should be true. Then a {@link MarkPassingUpdateListener} is initialized which puts new fixes into a queue
 * as {@link StorePositionUpdateStrategy}. A new thread will also be started to evaluate the new fixes (See
 * {@link CandidateFinder} and {@link CandidateChooser}). This continues until the {@link MarkPassingUpdateListener}
 * signals that the race is over (after {@link #stop()} is called.
 * 
 * @author Nicolas Klose
 * @author Axel Uhl (d043530)
 * 
 */
public class MarkPassingCalculator {
    private final DynamicTrackedRace race;
    private CandidateFinder finder;
    private CandidateChooser chooser;
    private static final Logger logger = Logger.getLogger(MarkPassingCalculator.class.getName());
    private final MarkPassingUpdateListener listener;
    private final static ExecutorService executor = ThreadPoolUtil.INSTANCE.getDefaultBackgroundTaskThreadPoolExecutor();
    private final LinkedBlockingQueue<StorePositionUpdateStrategy> queue;
    
    /**
     * An "end marker" that can be {@link #enqueueUpdate(StorePositionUpdateStrategy) enqueued} in order to tell the {@link Listen}
     * thread to stop.
     */
    private final StorePositionUpdateStrategy endMarker = new StorePositionUpdateStrategy() {
        @Override
        public void storePositionUpdate(Map<Competitor, List<GPSFixMoving>> competitorFixes,
                Map<Competitor, List<GPSFixMoving>> competitorFixesThatReplacedExistingOnes, Map<Mark, List<GPSFix>> markFixes, List<Waypoint> addedWaypoints,
                List<Waypoint> removedWaypoints, IntHolder smallestChangedWaypointIndex,
                List<Triple<Competitor, Integer, TimePoint>> fixedMarkPassings,
                List<Pair<Competitor, Integer>> removedMarkPassings, List<Pair<Competitor, Integer>> suppressedMarkPassings, List<Competitor> unSuppressedMarkPassings, CandidateFinder candidateFinder, CandidateChooser candidateChooser) {
        }
    };

    private boolean suspended = false;
    
    /**
     * If the constructor is called with the {@code listen} parameter set to {@code true}, this field will hold a thread
     * when objects were added to the {@link #queue} and are being processed. When the thread has completed processing
     * all updates from the {@link #queue} it will terminate and set this field to {@code null}. The decision to create
     * and stop the thread goes hand in hand with setting or, respectively, clearing this field while holding this object's
     * monitor ({@code synchronized}).<p>
     * 
     * The thread may not yet have been started because, depending on the
     * constructor's {@code waitForInitialMarkPassingCalculation} argument, start-up may be asynchronous, but eventually
     * it will be started at some point. The {@link #waitUntilStopped} method can be used to wait until the thread has
     * started and then terminated again.
     */
    private Thread listenerThread;

    private final Listen listen;
    
    /**
     * Synchronized using the {@link #listenerThread} as monitor object.
     */
    private boolean listenerThreadStarted;

    public MarkPassingCalculator(DynamicTrackedRace race, boolean doListen, boolean waitForInitialMarkPassingCalculation) {
        if (doListen) {
            queue = new LinkedBlockingQueue<>();
            listener = new MarkPassingUpdateListener(race, this);
        } else {
            listener = null;
            queue = null; // no queue is needed if no change listener is in use
        }
        this.race = race;
        finder = new CandidateFinderImpl(race, executor);
        chooser = new CandidateChooserImpl(race);
        if (listener != null) {
            listen = new Listen();
        } else {
            listen = null;
        }
        Thread t = new Thread(() -> {
            final Set<Callable<Void>> tasks = new HashSet<>();
            for (Competitor c : race.getRace().getCompetitors()) {
                tasks.add(()->{
                    Util.Pair<Iterable<Candidate>, Iterable<Candidate>> allCandidates = finder.getAllCandidates(c);
                    chooser.calculateMarkPassDeltas(c, allCandidates.getA(), allCandidates.getB());
                    return null;
                });
            }
            try {
                executor.invokeAll(tasks);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error trying to compute initial set of mark passings for race "+race.getRace().getName(), e);
            }
            if (listener != null) {
                synchronized (MarkPassingCalculator.this) {
                    if (listenerThread == null) {
                        listenerThread = createAndStartListenerThread();
                        synchronized (listenerThread) {
                            listenerThreadStarted = true;
                            listenerThread.notifyAll();
                        }
                    }
                }
            }
        }, "MarkPassingCalculator for race "+race.getRace().getName()+" initialization");
        if (waitForInitialMarkPassingCalculation) {
            t.run();
        } else {
            t.start();
        }
    }

    private Thread createAndStartListenerThread() {
        final Thread result = new Thread(listen, "MarkPassingCalculator for race " + race.getRace().getName());
        result.setDaemon(true);
        result.start();
        return result;
    }
    
    /**
     * Waits for the listening thread to terminate. If no listening thread has been requested ({@code listen} constructor
     * parameter {@code false}), the method returns immediately. Otherwise it waits for the listening thread to have started
     * and then joins on the thread, using the timeout specified.<p>
     * 
     * This method can be useful for test cases that want to wait until all calculations have terminated for sure.
     * 
     * @param timeoutInMillis same as for {@link Thread#join(long)}.
     */
    public void waitUntilStopped(final long timeoutInMillis) throws InterruptedException {
        final long start = System.currentTimeMillis();
        if (listenerThread != null) {
            synchronized (listenerThread) {
                while (!listenerThreadStarted && System.currentTimeMillis()-start < timeoutInMillis) {
                    listenerThread.wait(timeoutInMillis);
                }
            }
            listenerThread.join(timeoutInMillis);
        }
    }

    /**
     * It's used for locking the calculation thread for read. You will be blocked if the calculation is in progress.
     * Make sure that you unlock the thread with the method {@link MarkPassingCalculator#unlockForRead()} once you don't
     * need it any more, using a {@code finally} clause to ensure unlocking even with abnormal termination.
     */
    public void lockForRead() {
        if (listen != null) {
            listen.lockForRead();
        }
    }
    
    /**
     * Unlocks the calculation thread for read.
     * 
     * @see #lockForRead
     */
    public void unlockForRead() {
        if (listen != null) {
            listen.unlockAfterRead();
        }
    }

    /**
     * Waits until an object is in the queue, then drains it entirely. After that the information is sorted depending on
     * whether it is a fix, an updated waypoint or an updated fixed markpassing. Finally, if <code>suspended</code> is
     * false, new passing are computed. First new wayponts are evaluated, than new fixed markpassings. Than the
     * markpassings are computed using mark and competitor fixes.
     * 
     * @author Nicolas Klose
     * 
     */
    private class Listen implements Runnable {
        private final String raceName;
        
        /**
         * The write lock is being held by the thread running this {@link Runnable} after successfully having
         * waited for the next update to be pushed to the {@link MarkPassingUpdateListener#getQueue() queue},
         * before draining the queue starts and until the processing of all events from the queue has completed.
         * This way, trying to obtain the read lock will block until all pending updates have been processed.
         */
        private final NamedReentrantReadWriteLock lock;
        
        public Listen() {
            this.raceName = race.getRace().getName();
            lock = new NamedReentrantReadWriteLock("lock for calculation thread (" + Listen.class.getSimpleName() + ")", /* fair */ false);
        }
        
        public void lockForRead() {
            LockUtil.lockForRead(lock);
        }
        
        public void unlockAfterRead() {
            LockUtil.unlockAfterRead(lock);
        }

        @Override
        public void run() {
            try {
                logger.fine("MarkPassingCalculator is listening on race "+raceName);
                boolean finished = false;
                final Map<Competitor, List<GPSFixMoving>> competitorFixes = new HashMap<>();
                final Map<Competitor, List<GPSFixMoving>> competitorFixesThatReplacedExistingOnes = new HashMap<>();
                final Map<Mark, List<GPSFix>> markFixes = new HashMap<>();
                final List<Waypoint> addedWaypoints = new ArrayList<>();
                final List<Waypoint> removedWaypoints = new ArrayList<>();
                final IntHolder smallestChangedWaypointIndex = new IntHolder(-1);
                final List<Triple<Competitor, Integer, TimePoint>> fixedMarkPassings = new ArrayList<>();
                final List<Pair<Competitor, Integer>> removedFixedMarkPassings = new ArrayList<>();
                final List<Pair<Competitor, Integer>> suppressedMarkPassings = new ArrayList<>();
                final List<Competitor> unsuppressedMarkPassings = new ArrayList<>();
                boolean hasUnprocessedUpdates = false;
                while (!finished) {
                    try {
                        logger.finer("MPC for "+raceName+" is checking the queue");
                        synchronized (MarkPassingCalculator.this) {
                            // check if queue is empty while owning the MarkPassingCalculator's monitor;
                            // if the queue is empty, set listenerThread to null while under the monitor
                            // and decide to finish the thread; otherwise, take the next object
                            if (!hasUnprocessedUpdates && queue.isEmpty()) {
                                finished = true;
                                listenerThread = null;
                            }
                        }
                        LockUtil.lockForWrite(lock);
                        if (!finished) {
                            List<StorePositionUpdateStrategy> allNewFixInsertions = new ArrayList<>();
                            try {
                                allNewFixInsertions.add(queue.take());
                            } catch (InterruptedException e) {
                                logger.log(Level.SEVERE, "MarkPassingCalculator for "+raceName+" threw exception " + e.getMessage()
                                        + " while waiting for new GPSFixes");
                            }
                            queue.drainTo(allNewFixInsertions);
                            logger.fine("MPC for "+raceName+" received "+ allNewFixInsertions.size()+" new updates.");
                            for (StorePositionUpdateStrategy fixInsertion : allNewFixInsertions) {
                                if (isEndMarker(fixInsertion)) {
                                    logger.fine("Stopping "+MarkPassingCalculator.this+"'s listener for race "+raceName);
                                    finished = true;
                                    break;
                                } else {
                                    try {
                                        fixInsertion.storePositionUpdate(competitorFixes, competitorFixesThatReplacedExistingOnes,
                                                markFixes, addedWaypoints,
                                                removedWaypoints, smallestChangedWaypointIndex, fixedMarkPassings,
                                                removedFixedMarkPassings, suppressedMarkPassings, unsuppressedMarkPassings, finder, chooser);
                                        hasUnprocessedUpdates = true;
                                    } catch (Exception e) {
                                        logger.log(Level.SEVERE, "Error while calculating markpassings for race "+raceName+
                                                " while applying update " +fixInsertion+": "+ e.getMessage()+
                                                ". Continuing with further updates...", e);
                                    }
                                }
                            }
                        }
                        // in suspended mode we've stored the update contents in competitorFixes, markFixes, etc. but
                        // haven't processed them yet with the CandidateFinder or CandidateChooser.
                        if (!finished && !suspended) {
                            if (smallestChangedWaypointIndex.value != -1) {
                                // there was a course change; finder and chooser need to be updated in this regard;
                                // do this before the other changes such as new / replaced position fixes are considered:
                                final Course course = race.getRace().getCourse();
                                final Map<Competitor, Util.Pair<List<Candidate>, List<Candidate>>> candidateDeltas;
                                // obtain the course's read lock so that the finder creates the candidate deltas under the
                                // premise of the same course as the chooser updates its end proxy node's index; they have
                                // to be consistent. See also bug 3657.
                                course.lockForRead();
                                try {
                                    candidateDeltas = finder.updateWaypoints(addedWaypoints, removedWaypoints, smallestChangedWaypointIndex.value);
                                    chooser.updateEndProxyNodeWaypointIndex();
                                } finally {
                                    course.unlockAfterRead();
                                }
                                if (!removedWaypoints.isEmpty()) {
                                    chooser.removeWaypoints(removedWaypoints);
                                }
                                Set<Callable<Void>> tasks = new HashSet<>();
                                for (Entry<Competitor, Util.Pair<List<Candidate>, List<Candidate>>> entry : candidateDeltas.entrySet()) {
                                    tasks.add(()->{
                                        Util.Pair<List<Candidate>, List<Candidate>> pair = entry.getValue();
                                        chooser.calculateMarkPassDeltas(entry.getKey(), pair.getA(), pair.getB());
                                        return null;
                                    });
                                }
                                executor.invokeAll(tasks);
                            }
                            updateManuallySetMarkPassings(fixedMarkPassings, removedFixedMarkPassings, suppressedMarkPassings, unsuppressedMarkPassings);
                            computeMarkPasses(competitorFixes, competitorFixesThatReplacedExistingOnes, markFixes);
                            competitorFixes.clear();
                            competitorFixesThatReplacedExistingOnes.clear();
                            markFixes.clear();
                            addedWaypoints.clear();
                            removedWaypoints.clear();
                            smallestChangedWaypointIndex.value = -1;
                            fixedMarkPassings.clear();
                            removedFixedMarkPassings.clear();
                            suppressedMarkPassings.clear();
                            unsuppressedMarkPassings.clear();
                            hasUnprocessedUpdates = false;
                        }
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error while calculating markpassings for race "+raceName+": " + e.getMessage(), e);
                    } finally {
                        LockUtil.unlockAfterWrite(lock);
                    }
                }
            } finally {
                logger.fine("MarkPassingCalculator Listen thread terminating for race "+raceName);
            }
        }

        private void updateManuallySetMarkPassings(List<Triple<Competitor, Integer, TimePoint>> fixedMarkPassings,
                List<Pair<Competitor, Integer>> removedMarkPassings,
                List<Pair<Competitor, Integer>> suppressedMarkPassings, List<Competitor> unsuppressedMarkPassings) {
            logger.finest("Updating manually edited MarkPassings for race "+raceName);
            for (Pair<Competitor, Integer> pair : suppressedMarkPassings) {
                chooser.suppressMarkPassings(pair.getA(), pair.getB());
            }
            for (Competitor c : unsuppressedMarkPassings) {
                chooser.stopSuppressingMarkPassings(c);
            }

            for (Pair<Competitor, Integer> pair : removedMarkPassings) {
                chooser.removeFixedPassing(pair.getA(), pair.getB());
            }
            for (Triple<Competitor, Integer, TimePoint> triple : fixedMarkPassings) {
                chooser.setFixedPassing(triple.getA(), triple.getB(), triple.getC());
            }
        }

        /**
         * The calculation has two steps. For every mark with new fixes those competitor fixes are calculated that may
         * have changed their status as a {@link Candidate} (see {@code FixesAffectedByNewMarkFixes}). These fixes,
         * along with any new competitor fixes, are passed into the executor, one task per competitor. The
         * {@link CandidateFinder} uses the fixes to calculate any new or wrong Candidates. These are passed to the
         * {@link CandidateChooser} to calculate any new {@link MarkPassing}s (see {@link ComputeMarkPassings}).
         */
        private void computeMarkPasses(final Map<Competitor, List<GPSFixMoving>> newCompetitorFixes,
                final Map<Competitor, List<GPSFixMoving>> competitorFixesThatReplacedExistingOnes, Map<Mark, List<GPSFix>> newMarkFixes) {
            logger.finer("Calculating markpassings for race "+raceName+" with " + newCompetitorFixes.size() + " new competitor Fixes and "
                    + newMarkFixes.size() + " new mark fixes.");
            Map<Competitor, Collection<GPSFixMoving>> combinedCompetitorFixesFinderConsidersAffected = new HashMap<>();
            for (Entry<Competitor, List<GPSFixMoving>> competitorEntry : newCompetitorFixes.entrySet()) {
                Collection<GPSFixMoving> fixesForCompetitor = new LinkedList<>();
                combinedCompetitorFixesFinderConsidersAffected.put(competitorEntry.getKey(), fixesForCompetitor);
                fixesForCompetitor.addAll(competitorEntry.getValue());
            }
            if (!newMarkFixes.isEmpty()) {
                // FIXME bug 2745 use new mark fixes to invalidate chooser's mark position and mutual mark/waypoint distance cache
                for (Entry<Competitor, List<GPSFixMoving>> fixesAffectedByNewMarkFixes : finder
                        .calculateFixesAffectedByNewMarkFixes(newMarkFixes).entrySet()) {
                    Collection<GPSFixMoving> fixes = combinedCompetitorFixesFinderConsidersAffected.get(fixesAffectedByNewMarkFixes.getKey());
                    if (fixes == null) {
                        fixes = new LinkedList<>();
                        combinedCompetitorFixesFinderConsidersAffected.put(fixesAffectedByNewMarkFixes.getKey(), fixes);
                    }
                    fixes.addAll(fixesAffectedByNewMarkFixes.getValue());
                }
            }
            List<Callable<Void>> tasks = new ArrayList<>();
            for (final Entry<Competitor, Collection<GPSFixMoving>> competitorAndFixesFinderConsidersAffected : combinedCompetitorFixesFinderConsidersAffected.entrySet()) {
                final Runnable runnable = new ComputeMarkPassings(competitorAndFixesFinderConsidersAffected.getKey(),
                        competitorAndFixesFinderConsidersAffected.getValue(),
                        newCompetitorFixes.get(competitorAndFixesFinderConsidersAffected.getKey()),
                        competitorFixesThatReplacedExistingOnes.get(competitorAndFixesFinderConsidersAffected.getKey()));
                tasks.add(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        runnable.run();
                        return null;
                    }
                    
                    /**
                     * A reasonable toString implementation helps identifying these tasks in log messages for long-running tasks
                     */
                    @Override
                    public String toString() {
                        return "Mark passing calculation for competitor "+competitorAndFixesFinderConsidersAffected.getKey()+" with "+competitorAndFixesFinderConsidersAffected.getValue().size()+" fixes";
                    }
                });
            }
            try {
                executor.invokeAll(tasks);
            } catch (InterruptedException e) {
                logger.log(Level.INFO, "mark passing calculation interrupted", e);
            }
        }

        private class ComputeMarkPassings implements Runnable {
            private final Competitor c;
            private final Iterable<GPSFixMoving> fixesFinderConsidersAffected;
            private final List<GPSFixMoving> newCompetitorFixes;
            private final List<GPSFixMoving> competitorFixesThatReplacedExistingOnes;

            public ComputeMarkPassings(Competitor c, Iterable<GPSFixMoving> fixesFinderConsidersAffected,
                    List<GPSFixMoving> newCompetitorFixes,
                    List<GPSFixMoving> competitorFixesThatReplacedExistingOnes) {
                this.c = c;
                this.fixesFinderConsidersAffected = fixesFinderConsidersAffected;
                this.newCompetitorFixes = newCompetitorFixes;
                this.competitorFixesThatReplacedExistingOnes = competitorFixesThatReplacedExistingOnes;
            }

            @Override
            public void run() {
                try {
                    logger.finer(()->"Calculating MarkPassings for race "+raceName+", competitor " + c + " (" + Util.size(fixesFinderConsidersAffected) + " new fixes)");
                    Util.Pair<Iterable<Candidate>, Iterable<Candidate>> candidateDeltas = finder.getCandidateDeltas(c, fixesFinderConsidersAffected);
                    logger.finer(()->"Received " + Util.size(candidateDeltas.getA()) + " new Candidates for race "+raceName+
                        " and competitor "+c+" and will remove "
                        + Util.size(candidateDeltas.getB()) + " old Candidates for " + c);
                    chooser.calculateMarkPassDeltas(c,
                            newCompetitorFixes, competitorFixesThatReplacedExistingOnes,
                            candidateDeltas.getA(), candidateDeltas.getB());
                } catch (Exception e) {
                    // make sure the exception is logged and not only "swallowed" and suppressed by an invokeAll(...) statement
                    logger.log(Level.SEVERE, "Exception trying to compute mark passings for competitor "+c, e);
                    throw e;
                }
            }
        }

        @Override
        public String toString() {
            return getClass().getName()+" for race "+raceName;
        }
    }

    /**
     * Only suspends the actual calculation. Even when suspended all incoming fixes are added to the queue and sorted.
     */
    public void suspend() {
        synchronized(this) {
            logger.finest("Suspended MarkPassingCalculator");
            suspended = true;
        }
    }

    /**
     * An empty object is written to the queue to ensure that any fixes that have been removed from the queue are
     * evaluated even if nothing else arrives after this is called.
     */
    public void resume() {
        logger.finest("Resumed MarkPassingCalculator");
        synchronized (this) {
            suspended = false;
            enqueueUpdate(new StorePositionUpdateStrategy() {
                @Override
                public void storePositionUpdate(Map<Competitor, List<GPSFixMoving>> competitorFixes,
                        Map<Competitor, List<GPSFixMoving>> competitorFixesThatReplacedExistingOnes, Map<Mark, List<GPSFix>> markFixes, List<Waypoint> addedWaypoints,
                        List<Waypoint> removedWaypoints,
                        IntHolder smallestChangedWaypointIndex,
                        List<Triple<Competitor, Integer, TimePoint>> fixedMarkPassings,
                        List<Pair<Competitor, Integer>> removedMarkPassings, List<Pair<Competitor, Integer>> suppressedMarkPassings, List<Competitor> unSuppressedMarkPassings, CandidateFinder candidateFinder, CandidateChooser candidateChooser) {
                }
            });
        }
    }
    
    private boolean isEndMarker(StorePositionUpdateStrategy endMarkerCandidate) {
        return endMarkerCandidate == endMarker;
    }

    // protected for test case access
    protected void enqueueUpdate(StorePositionUpdateStrategy update) {
        synchronized (this) {
            queue.add(update);
            // regardless of whether the queue is empty or not, launch the thread if it doesn't run yet and we are not suspended;
            // the queue may have filled up while we were suspended
            if (!suspended && listenerThread == null) {
                listenerThread = createAndStartListenerThread();
            }
        }
    }

    public void stop() {
        logger.info("Stopping " + this + " for race " + race.getRace().getName());
        enqueueUpdate(endMarker);
    }

    public void recalculateEverything() {
        finder = new CandidateFinderImpl(race, executor);
        chooser = new CandidateChooserImpl(race);
        for (Competitor c : race.getRace().getCompetitors()) {
            Util.Pair<Iterable<Candidate>, Iterable<Candidate>> allCandidates = finder.getAllCandidates(c);
            chooser.calculateMarkPassDeltas(c, allCandidates.getA(), allCandidates.getB());
        }
    }

    public MarkPassingUpdateListener getListener() {
        return listener;
    }
    
    @Override
    public String toString() {
        return getClass().getName()+" for "+race+" with chooser "+chooser;
    }
}