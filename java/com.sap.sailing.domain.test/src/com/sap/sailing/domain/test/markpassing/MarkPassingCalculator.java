package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.maptrack.utils.Pair;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.util.impl.ThreadFactoryWithPriority;

public class MarkPassingCalculator {
    private CandidateFinder finder;
    private CandidateChooser chooser;
    private LinkedBlockingQueue<Pair<Object, GPSFix>> queue;
    private boolean suspended = false;
    private final static Executor recalculator = new ThreadPoolExecutor(/* corePoolSize */Math.max(Runtime.getRuntime()
            .availableProcessors() - 1, 3),
    /* maximumPoolSize */Math.max(Runtime.getRuntime().availableProcessors() - 1, 3),
    /* keepAliveTime */60, TimeUnit.SECONDS,
    /* workQueue */new LinkedBlockingQueue<Runnable>(), new ThreadFactoryWithPriority(Thread.NORM_PRIORITY - 1));

    public MarkPassingCalculator(DynamicTrackedRace race, boolean listen) {
        finder = new CandidateFinder(race);
        chooser = new CandidateChooser(race, finder.getAllCandidates());

        if (listen) {
            MarkPassingUpdateListener listener = new MarkPassingUpdateListener(race, this);
            queue = listener.getQueue();
            // Multi-Threading
            recalculator.execute(new Listen());
        }
    }

    private class Listen implements Runnable {

        @Override
        public void run() {

            boolean finished = false;
            List<Pair<Object, GPSFix>> allNewFixes = new ArrayList<Pair<Object, GPSFix>>();
            LinkedHashMap<Object, List<GPSFix>> combinedFixes = new LinkedHashMap<>();
            while (!finished) {
                try {
                    allNewFixes.add(queue.take());
                } catch (InterruptedException e) {
                }
                queue.drainTo(allNewFixes);
                for (Pair<Object, GPSFix> fix : allNewFixes) {
                    if (combinedFixes.keySet().contains(fix.first())) {
                        combinedFixes.get(fix.first()).add(fix.second());
                    } else {
                        combinedFixes.put(fix.first(), Arrays.asList(fix.second()));
                    }
                }
                if (!suspended) {
                    for (Object o : combinedFixes.keySet()) {
                        recalculator.execute(new getAffectedFixes(o, combinedFixes.get(o)));
                    }
                    for (Competitor c : finder.getAffectedCompetitors()) {
                        recalculator.execute(new ComputeMarkPassings(c));
                    }
                }
            }
        }
    }

    private class getAffectedFixes implements Runnable {
        Object o;
        List<GPSFix> fixes;
    
        public getAffectedFixes(Object o, List<GPSFix> fixes) {
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

    public LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> getMarkPasses() {
        return chooser.getAllMarkPasses();
    }
}