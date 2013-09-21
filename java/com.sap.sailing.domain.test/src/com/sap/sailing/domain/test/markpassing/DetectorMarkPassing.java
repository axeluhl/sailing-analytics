package com.sap.sailing.domain.test.markpassing;


import java.util.NavigableSet;

import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;


public interface DetectorMarkPassing {

    public NavigableSet<MarkPassing> computeMarkpasses(DynamicGPSFixTrack<Competitor, GPSFixMoving> gpsFixes, Iterable<Waypoint> waypoints);
     
}
