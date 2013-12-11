package com.sap.sailing.domain.test.markpassing;

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
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.util.impl.ThreadFactoryWithPriority;

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
            executor.execute(new ComputeMarkPassings(c, true));
        }
        if (listen) {
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
                FutureTask<Boolean> task = new FutureTask<>(new ComputeMarkPassings(c, false), true);
                tasks.add(task);
                executor.execute(task);
            }
            for (FutureTask<Boolean> task : tasks) {
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
                finder.calculateFixesAffectedByNewCompetitorFixes(fixes, (Competitor) o);
            } else if (o instanceof Mark) {
                finder.calculateFixesAffectedByNewMarkFixes((Mark) o, fixes);
            }
        }
    }

    private class ComputeMarkPassings implements Runnable {
        Competitor c;
        boolean reCalculateAllFixes;

        public ComputeMarkPassings(Competitor c, boolean reCalculateAllFixes) {
            this.c = c;
            this.reCalculateAllFixes = reCalculateAllFixes;
        }

        @Override
        public void run() {
            if (reCalculateAllFixes) {
                finder.reCalculateAllFixes(c);
            }
            chooser.calculateMarkPassDeltas(c, finder.getCandidateDeltas(c));
        }
    }

    public void suspend() {
        suspended = true;
    }

    public void resume() {
        suspended = false;
    }

}