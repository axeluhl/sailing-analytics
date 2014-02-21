package com.sap.sailing.domain.markpassingcalculation;

import java.util.concurrent.LinkedBlockingQueue;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
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
    private LinkedBlockingQueue<Pair<Object, GPSFix>> queue;
    private final Pair<Object, GPSFix> end = new Pair<>(null, null);

    /**
     * Adds itself automatically as a Listener on the <code>race</code>.
     */
    public MarkPassingUpdateListener(TrackedRace race) {
        queue = new LinkedBlockingQueue<>();
        race.addListener(this);
    }

    public LinkedBlockingQueue<Pair<Object, GPSFix>> getQueue() {
        return queue;
    }

    @Override
    public void competitorPositionChanged(GPSFixMoving fix, Competitor competitor) {
        queue.add(new Pair<Object, GPSFix>(competitor, fix));
    }

    @Override
    public void markPositionChanged(GPSFix fix, Mark mark) {
        queue.add(new Pair<Object, GPSFix>(mark, fix));
    }

    public boolean isEndMarker(Pair<Object, GPSFix> pair) {
        return pair == end;
    }

    @Override
    public void statusChanged(TrackedRaceStatus newStatus) {
        if (newStatus.getStatus() == TrackedRaceStatusEnum.FINISHED) {
            queue.add(end);
        }
    }
}
