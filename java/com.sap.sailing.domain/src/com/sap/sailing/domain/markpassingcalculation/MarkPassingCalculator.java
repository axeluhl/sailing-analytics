package com.sap.sailing.domain.markpassingcalculation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
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
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.util.impl.ThreadFactoryWithPriority;

/**
 * Calculates the {@link MarkPassing}s for a {@link DynamicTrackedRace} using a {@link AbstractCandidateFinder} and a
 * {@link AbstractCandidateChooser}. The finder evaluates the fixes and finds possible MarkPassings as {@link Candidate}s.
 * The chooser than finds the most likely sequence of {@link Candidate}s and creates the {@link MarkPassing}s for
 * this sequence. 
 * If <code>listen</code> is true, a {@link MarkPassingUpdateListener} is initialized which puts new
 * fixes into a queue. Additionally a new Thread is started, which takes the fixes out of the queue and sorts them so
 * that each object (Competitor or Mark) has a list of Fixes in <code>combinedFixes</code>. If <code>suspended</code> is
 * false, the new Fixes are handed to the <code>executor</code> as FutureTasks, first to calculate the affected Fixes
 * and then the actual MarkPassings (See {@link CandidateFinder} and {@link CandidateChooser}). Then the listening
 * process begins again with the queue being emptied. This continues until the <code>end</code> Object is put in the
 * queue by the {@link MarkPassingUpdateListener}, signalising that the race is over.
 * 
 * @author Nicolas Klose
 * 
 */

public class MarkPassingCalculator {
    private AbstractCandidateFinder finder;
    private AbstractCandidateChooser chooser;
    private static final Logger logger = Logger.getLogger(MarkPassingCalculator.class.getName());

    private MarkPassingUpdateListener listener;
    private final static Pair<Object, GPSFix> end = new Pair<Object, GPSFix>(null, null);
    private boolean suspended;
    private final static Executor executor = new ThreadPoolExecutor(/* corePoolSize */Math.max(Runtime.getRuntime()
            .availableProcessors() - 1, 3),
    /* maximumPoolSize */Math.max(Runtime.getRuntime().availableProcessors() - 1, 3),
    /* keepAliveTime */60, TimeUnit.SECONDS,
    /* workQueue */new LinkedBlockingQueue<Runnable>(), new ThreadFactoryWithPriority(Thread.NORM_PRIORITY - 1));

    public MarkPassingCalculator(DynamicTrackedRace race, boolean listen) {
        if (listen) {
            suspended = true;
            listener = new MarkPassingUpdateListener(race, end);
        }
        finder = new CandidateFinder(race);
        chooser = new CandidateChooser(race);
        for (Competitor c : race.getRace().getCompetitors()) {
            chooser.calculateMarkPassDeltas(c, finder.getAllCandidates(c));
        }
        if (listen) {
            suspended = false;
            new Thread(new Listen(), "MarkPassingCalculator listener for race " + race.getRace().getName()).start();
        }
    }

    private class Listen implements Runnable {

        @Override
        public void run() {

            boolean finished = false;
            LinkedHashMap<Object, List<GPSFix>> combinedFixes = new LinkedHashMap<>();

            while (!finished) {
                List<Pair<Object, GPSFix>> allNewFixes = new ArrayList<Pair<Object, GPSFix>>();
                try {
                    allNewFixes.add(listener.getQueue().take());
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "MarkPassingCalculator threw exception " + e.getMessage()
                            + " while waiting for new GPSFixes");
                }
                listener.getQueue().drainTo(allNewFixes);
                for (Pair<Object, GPSFix> fix : allNewFixes) {
                    if (fix == end) {
                        finished = true;
                        continue;
                    } else if (!combinedFixes.containsKey(fix.getA())) {
                        combinedFixes.put(fix.getA(), new ArrayList<GPSFix>());
                    }
                    combinedFixes.get(fix.getA()).add(fix.getB());
                }
                if (!suspended) {
                    computeMarkPasses(combinedFixes);
                    combinedFixes.clear();
                }
            }
        }

        private void computeMarkPasses(LinkedHashMap<Object, List<GPSFix>> combinedFixes) {

            List<FutureTask<Boolean>> tasks = new ArrayList<>();
            for (Object o : combinedFixes.keySet()) {
                FutureTask<Boolean> task = new FutureTask<>(new GetAffectedFixes(o, combinedFixes.get(o)), true);
                tasks.add(task);
                executor.execute(task);
            }
            for (FutureTask<Boolean> task : tasks) {
                try {
                    task.get();
                } catch (InterruptedException | ExecutionException e) {
                    logger.log(Level.SEVERE, "MarkPassingCalculator threw exception " + e.getMessage()
                            + "while waiting for the calculation of affected Fixes");
                }
            }
            tasks.clear();
            for (Competitor c : finder.getAffectedCompetitors()) {
                FutureTask<Boolean> task = new FutureTask<>(new ComputeMarkPassings(c), true);
                tasks.add(task);
            }
            for (FutureTask<Boolean> task : tasks) {
                executor.execute(task);
                try {
                    task.get();
                } catch (InterruptedException | ExecutionException e) {
                    logger.log(Level.SEVERE, "MarkPassingCalculator threw exception " + e.getMessage()
                            + "while waiting for the calculation of MarkPassings");
                }
            }
        }
    }

    private class GetAffectedFixes implements Runnable {
        Object o;
        List<GPSFix> fixes;

        public GetAffectedFixes(Object o, List<GPSFix> fixes) {
            this.o = o;
            this.fixes = fixes;
        }

        @Override
        public void run() {
            if (o instanceof Competitor) {
                finder.calculateFixesAffectedByNewCompetitorFixes((Competitor) o, fixes);
            } else if (o instanceof Mark) {
                finder.calculateFixesAffectedByNewMarkFixes((Mark) o, fixes);
            }
        }
    }

    private class ComputeMarkPassings implements Runnable {
        Competitor c;

        public ComputeMarkPassings(Competitor c) {
            this.c = c;
        }

        @Override
        public void run() {
            chooser.calculateMarkPassDeltas(c, finder.getCandidateDeltas(c));
        }
    }

    public void suspend() {
        suspended = true;
    }

    public void resume() {
        suspended = false;
    }

    public LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> getAllPasses() {
        return chooser.getAllPasses();
    }
}