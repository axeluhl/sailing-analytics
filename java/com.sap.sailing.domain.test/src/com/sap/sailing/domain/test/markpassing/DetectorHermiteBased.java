package com.sap.sailing.domain.test.markpassing;

import java.util.NavigableSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;

public class DetectorHermiteBased implements DetectorMarkPassing {

    @Override
    public NavigableSet<MarkPassing> computeMarkpasses(DynamicGPSFixTrack<Competitor, GPSFixMoving> gpsFixes,
            Iterable<Waypoint> waypoints) {
        // TODO Auto-generated method stub
        return null;
    }

}