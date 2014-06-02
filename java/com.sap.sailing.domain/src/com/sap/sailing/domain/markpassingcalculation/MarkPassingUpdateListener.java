package com.sap.sailing.domain.markpassingcalculation;

import java.util.concurrent.LinkedBlockingQueue;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sse.common.UtilNew;

/**
 * Listens for changes that might affect the MarkPassingCalculator: new Fixes of a Competitor or a Mark and when the
 * race status changes to finished. New Fixes are put in queue to be evaluated by the {@link MarkPassingCalculator} and
 * the <code>end</code> object is passed through to signalise the end of the race.
 * 
 * @author Nicolas Klose
 * 
 */
public class MarkPassingUpdateListener extends AbstractRaceChangeListener {
    private final LinkedBlockingQueue<UtilNew.Pair<Object, GPSFix>> queue;
    private final UtilNew.Pair<Object, GPSFix> end;

    public MarkPassingUpdateListener(TrackedRace race, UtilNew.Pair<Object, GPSFix> end) {
        race.addListener(this);
        queue = new LinkedBlockingQueue<>();
        this.end = end;
    }

    public LinkedBlockingQueue<UtilNew.Pair<Object, GPSFix>> getQueue() {
        return queue;
    }

    @Override
    public void competitorPositionChanged(GPSFixMoving fix, Competitor item) {
        queue.add(new UtilNew.Pair<Object, GPSFix>(item, fix));
    }

    @Override
    public void markPositionChanged(GPSFix fix, Mark mark) {
        queue.add(new UtilNew.Pair<Object, GPSFix>(mark, fix));
    }

    @Override
    public void statusChanged(TrackedRaceStatus newStatus) {
        if (newStatus.getStatus() == TrackedRaceStatusEnum.FINISHED) {
            queue.add(end);
        }
    }
}
