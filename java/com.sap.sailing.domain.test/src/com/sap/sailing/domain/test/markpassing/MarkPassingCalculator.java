package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.maptrack.utils.Pair;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.MarkPassing;

public class MarkPassingCalculator {
    private CandidateFinder finder;
    private CandidateChooser chooser;
    private LinkedBlockingQueue<Pair<Object, GPSFix>> queue;
    private boolean suspended = false;

    public MarkPassingCalculator(DynamicTrackedRace race, boolean listen) {
        if (race.getStatus().getStatus() == TrackedRaceStatusEnum.LOADING) {
            suspended = true;
        }
        while (suspended) {
        }
        finder = new CandidateFinder(race);
        chooser = new CandidateChooser(race, finder.getAllCandidates());
        if (listen) {
            MarkPassingUpdateListener listener = new MarkPassingUpdateListener(race);
            queue = listener.getQueue();
            startListening();
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

    public void calculateMarkPassDeltas(
            Pair<LinkedHashMap<Competitor, List<GPSFix>>, LinkedHashMap<Mark, List<GPSFix>>> fixes) {
        chooser.calculateMarkPassDeltas(finder.getCandidateDeltas(fixes));
    }

    public void startListening() {
        boolean finished = false;

        while (!finished) {
            List<Pair<Object, GPSFix>> allNewFixes = new ArrayList<Pair<Object, GPSFix>>();
            Pair<LinkedHashMap<Competitor, List<GPSFix>>, LinkedHashMap<Mark, List<GPSFix>>> sortedFixes = new Pair<LinkedHashMap<Competitor, List<GPSFix>>, LinkedHashMap<Mark, List<GPSFix>>>(
                    new LinkedHashMap<Competitor, List<GPSFix>>(), new LinkedHashMap<Mark, List<GPSFix>>());

            while (true) {
                try {
                    allNewFixes.add(queue.take());
                } catch (InterruptedException e) {
                }
                queue.drainTo(allNewFixes);

                for (Pair<Object, GPSFix> fix : allNewFixes) {
                    if (fix.first() instanceof Competitor) {
                        if (!sortedFixes.first().containsKey(fix.first())) {
                            sortedFixes.first().put((Competitor) fix.first(), new ArrayList<GPSFix>());
                        }
                        sortedFixes.first().get(fix.first()).add(fix.second());
                    } else if (fix.first() instanceof Mark) {
                        if (!sortedFixes.second().containsKey(fix.first())) {
                            sortedFixes.second().put((Mark) fix.first(), new ArrayList<GPSFix>());
                        }
                        sortedFixes.second().get(fix.first()).add(fix.second());
                    } else {
                        finished = true;
                    }
                }
                if (!suspended) {
                    break;
                }
            }
            chooser.calculateMarkPassDeltas(finder.getCandidateDeltas(sortedFixes));
        }
    }
}