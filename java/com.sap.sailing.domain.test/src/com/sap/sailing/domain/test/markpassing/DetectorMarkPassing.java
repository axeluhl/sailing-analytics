package com.sap.sailing.domain.test.markpassing;


import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;


public interface DetectorMarkPassing {

    public LinkedHashMap<ControlPoint, ArrayList<LinkedHashMap<GPSFixMoving, Double>>> findCandidates
    (ArrayList<GPSFixMoving> gpsFixes, 
     LinkedHashMap<ControlPoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>> controlPointTracks);
     
}
