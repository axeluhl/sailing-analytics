package com.sap.sailing.domain.test.markpassing;


import java.util.ArrayList;
import java.util.LinkedHashMap;


import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;


public interface DetectorMarkPassing {

    public LinkedHashMap<Waypoint, MarkPassing> computeMarkpasses
    (DynamicGPSFixTrack<Competitor, GPSFixMoving> gpsFixes, 
     LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>> markPasses, 
     TimePoint raceStart);
     
}
