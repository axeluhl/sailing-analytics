package com.sap.sailing.domain.test.markpassing;

import java.util.concurrent.LinkedBlockingQueue;

import com.maptrack.utils.Pair;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;

public class MarkPassingUpdateListener extends AbstractRaceChangeListener {
    private LinkedBlockingQueue<Pair<Object, GPSFix>> queue;
    
    public LinkedBlockingQueue<Pair<Object, GPSFix>> getQueue() {
        return queue;
    }

    @Override
    public void competitorPositionChanged(GPSFixMoving fix, Competitor item) {
        queue.add(new Pair<Object, GPSFix>(item, fix));
    }
    
    @Override
    public void markPositionChanged(GPSFix fix, Mark mark) {
        queue.add(new Pair<Object, GPSFix>(mark, fix));
    }

}
