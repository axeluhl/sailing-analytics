package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.LinkedHashMap;


import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;

public class DetectorHermiteBased implements DetectorMarkPassing {

    @Override
    public LinkedHashMap<Waypoint, MarkPassing> computeMarkpasses(
            DynamicGPSFixTrack<Competitor, GPSFixMoving> gpsFixes,
            LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>> markPasses, TimePoint raceStart) {
        // TODO Auto-generated method stub
        return null;
    }

    


}