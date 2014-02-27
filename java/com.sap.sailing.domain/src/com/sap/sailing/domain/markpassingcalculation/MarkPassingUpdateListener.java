package com.sap.sailing.domain.markpassingcalculation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.markpassingcalculation.impl.StorePositionUpdateStrategy;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;

/**
 * Listens for changes that might affect the MarkPassingCalculator: new Fixes of a Competitor or a Mark and when the
 * race status changes to finished. New Fixes are put in queue to be evaluated by the {@link MarkPassingCalculator} and
 * the <code>end</code> object is passed through to signal the end of the race.
 * 
 * @author Nicolas Klose
 * 
 */
public class MarkPassingUpdateListener extends AbstractRaceChangeListener {
    private LinkedBlockingQueue<StorePositionUpdateStrategy> queue;
    private final StorePositionUpdateStrategy endMarker = new StorePositionUpdateStrategy() {
        @Override
        public void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes, Map<Mark, List<GPSFix>> markFixes) {
        }
    };

    /**
     * Adds itself automatically as a Listener on the <code>race</code>.
     */
    public MarkPassingUpdateListener(TrackedRace race) {
        queue = new LinkedBlockingQueue<>();
        race.addListener(this);
    }

    public BlockingQueue<StorePositionUpdateStrategy> getQueue() {
        return queue;
    }

    @Override
    public void competitorPositionChanged(final GPSFixMoving fix, final Competitor competitor) {
        queue.add(new StorePositionUpdateStrategy() {
            @Override
            public void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes, Map<Mark, List<GPSFix>> markFixes) {
                List<GPSFix> list = competitorFixes.get(competitor);
                if (list == null) {
                    list = new ArrayList<>();
                    competitorFixes.put(competitor, list);
                }
                list.add(fix);
            }
        });
    }

    @Override
    public void markPositionChanged(final GPSFix fix, final Mark mark) {
        queue.add(new StorePositionUpdateStrategy() {
            @Override
            public void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes, Map<Mark, List<GPSFix>> markFixes) {
                List<GPSFix> list = markFixes.get(mark);
                if (list == null) {
                    list = new ArrayList<>();
                    markFixes.put(mark, list);
                }
                list.add(fix);
            }
        });
    }

    public boolean isEndMarker(StorePositionUpdateStrategy endMarkerCandidate) {
        return endMarkerCandidate == endMarker;
    }

}
