package com.sap.sailing.domain.markpassingcalculation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sse.common.Util;
import com.sap.sse.util.impl.ThreadFactoryWithPriority;

/**
 * Calculates the {@link MarkPassing}s for a {@link DynamicTrackedRace} using a {@link AbstractCandidateFinder} and a
 * {@link AbstractCandidateChooser}. The finder evaluates the fixes and finds possible MarkPassings as {@link Candidate}s.
 * The chooser then finds the most likely sequence of {@link Candidate}s and creates the {@link MarkPassing}s for
 * this sequence. If <code>listen</code> is true, a {@link MarkPassingUpdateListener} is initialized which puts new
 * fixes into a queue. Additionally a new thread is started, which takes the fixes out of the queue and sorts them so
 * that each object (Competitor or Mark) has a list of Fixes in <code>combinedFixes</code>. If <code>suspended</code> is
 * false, the new Fixes are handed to the <code>executor</code> as FutureTasks, first to calculate the affected Fixes
 * and then the actual MarkPassings (See {@link CandidateFinder} and {@link CandidateChooser}). Then the listening
 * process begins again with the queue being emptied. This continues until the <code>end</code> Object is put in the
 * queue by the {@link MarkPassingUpdateListener}, signaling that the race is over.
 * 
 * @author Nicolas Klose
 * 
 */
public class MarkPassingCalculator {
    private static final Logger logger = Logger.getLogger(MarkPassingCalculator.class.getName());
    private final static Util.Pair<Object, GPSFix> end = new Util.Pair<Object, GPSFix>(null, null);
    private final static ExecutorService executor = new ThreadPoolExecutor(/* corePoolSize */Math.max(Runtime.getRuntime()
            .availableProcessors() - 1, 3),
    /* maximumPoolSize */Math.max(Runtime.getRuntime().availableProcessors() - 1, 3),
    /* keepAliveTime */60, TimeUnit.SECONDS,
    /* workQueue */new LinkedBlockingQueue<Runnable>(), new ThreadFactoryWithPriority(Thread.NORM_PRIORITY - 1));

    private MarkPassingUpdateListener listener;
    private AbstractCandidateFinder finder;
    private AbstractCandidateChooser chooser;
    private boolean suspended = false;

    public MarkPassingCalculator(DynamicTrackedRace race, boolean listen) {
        if (listen) {
            listener = new MarkPassingUpdateListener(race, end);
        }
        finder = new CandidateFinder(race);
        chooser = new CandidateChooser(race);
        for (Competitor c : race.getRace().getCompetitors()) {
            chooser.calculateMarkPassDeltas(c, finder.getAllCandidates(c));
        }
        if (listen) {
            new Thread(new Listen(), "MarkPassingCalculator listener for race " + race.getRace().getName()).start();
        }
    }

    private class Listen implements Runnable {
        @Override
        public void run() {
            logger.fine("MarkPassingCalculator is listening for new Fixes.");
            boolean finished = false;
            LinkedHashMap<Object, List<GPSFix>> combinedFixes = new LinkedHashMap<>();
            while (!finished) {
                List<Util.Pair<Object, GPSFix>> allNewFixes = new ArrayList<Util.Pair<Object, GPSFix>>();
                try {
                    allNewFixes.add(listener.getQueue().take());
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "MarkPassingCalculator threw exception " + e.getMessage()
                            + " while waiting for new GPSFixes");
                }
                listener.getQueue().drainTo(allNewFixes);
                for (Util.Pair<Object, GPSFix> fix : allNewFixes) {
                    if (fix == end) {
                        finished = true;
                        continue;
                    }
                    if (!combinedFixes.containsKey(fix.getA())) {
                        combinedFixes.put(fix.getA(), new ArrayList<GPSFix>());
                    }
                    combinedFixes.get(fix.getA()).add(fix.getB());
                }
                if (!suspended) {
                    logger.finest("MarkPassingCalculator evaluating fixes for " + combinedFixes.size() + " objects.");
                    computeMarkPasses(combinedFixes);
                    combinedFixes.clear();
                }
            }
        }
        
        private void computeMarkPasses(LinkedHashMap<Object, List<GPSFix>> combinedFixes) {
            
            LinkedHashMap<Competitor, Set<GPSFix>> comFixes = new LinkedHashMap<>();
            Comparator<GPSFix> com = new Comparator<GPSFix>(){
                @Override
                public int compare(GPSFix arg0, GPSFix arg1) {
                    return arg0.getTimePoint().compareTo(arg1.getTimePoint());
                }
            };
            List<FutureTask<LinkedHashMap<Competitor, List<GPSFix>>>> markTasks = new ArrayList<>();
            for (Object o : combinedFixes.keySet()) {
                if (o instanceof Mark) {
                    FutureTask<LinkedHashMap<Competitor, List<GPSFix>>> task = new FutureTask<>(new FixesAffectedByNewMarkFixes((Mark)o, combinedFixes.get(o)));
                    markTasks.add(task);
                    executor.submit(task);
                }
                if (o instanceof Competitor){
                    if(!comFixes.keySet().contains(o)){
                        comFixes.put((Competitor) o, new TreeSet<GPSFix>(com));
                    }
                    comFixes.get(o).addAll(combinedFixes.get(o));
                }
            }
            for(FutureTask<LinkedHashMap<Competitor, List<GPSFix>>> task : markTasks){
                LinkedHashMap<Competitor, List<GPSFix>> fixes = new LinkedHashMap<>();
                try {
                    fixes = task.get();
                } catch (InterruptedException | ExecutionException e) {
                    logger.severe("Threw Exception " + e.getMessage()+" while calculating fixes affected by new Mark Fixes.");
                }
                for (Competitor c : fixes.keySet()){
                    if(!comFixes.keySet().contains(c)){
                        comFixes.put((Competitor) c, new TreeSet<GPSFix>(com));
                    }
                    comFixes.get(c).addAll(fixes.get(c));
                }
            }
            List<Callable<Object>> tasks = new ArrayList<>();
            for (Competitor c : comFixes.keySet()) {
                    tasks.add(Executors.callable(new ComputeMarkPassings(c, comFixes.get(c))));
            }
            try {
                executor.invokeAll(tasks);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private class ComputeMarkPassings implements Runnable {
            Competitor c;
            Set<GPSFix> fixes;
            public ComputeMarkPassings(Competitor c, Set<GPSFix> fixes) {
                this.c = c;
                this.fixes = fixes;
            }
            @Override
            public void run() {
                chooser.calculateMarkPassDeltas(c, finder.getCandidateDeltas(c, fixes));
            }
        }

        private class FixesAffectedByNewMarkFixes implements Callable<LinkedHashMap<Competitor, List<GPSFix>>> {
            Mark m;
            Iterable<GPSFix> fixes;
            public FixesAffectedByNewMarkFixes(Mark m, Iterable<GPSFix> fixes) {
                this.m = m;
                this.fixes = fixes;
            }
            @Override
            public LinkedHashMap<Competitor, List<GPSFix>> call() {
                 return finder.calculateFixesAffectedByNewMarkFixes((Mark) m, fixes);
            }
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