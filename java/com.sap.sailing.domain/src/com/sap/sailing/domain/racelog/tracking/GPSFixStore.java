package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;


public interface GPSFixStore {
    void loadTrack(DynamicGPSFixTrack<Competitor, GPSFixMoving> track, RaceLog raceLog,
            Competitor competitor);

    void loadTrack(DynamicGPSFixTrack<Mark, GPSFix> track, RaceLog raceLog, Mark mark);

    void storeFix(DeviceIdentifier device, GPSFix fix);
}
