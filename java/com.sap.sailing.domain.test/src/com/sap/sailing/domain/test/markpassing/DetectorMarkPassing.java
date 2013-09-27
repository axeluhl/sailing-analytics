package com.sap.sailing.domain.test.markpassing;


import java.util.ArrayList;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;


public interface DetectorMarkPassing {

    public TimePoint computeMarkpass
    (ArrayList<GPSFixMoving> gpsFixes, 
     ArrayList<DynamicGPSFixTrack<Mark, GPSFix>> markPasses, 
     TimePoint previousMarkPassing);
     
}
