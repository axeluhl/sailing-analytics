package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasGPSFixTrackContext;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;

public class GPSFixTrackWithContext implements HasGPSFixTrackContext {

    private final HasRaceOfCompetitorContext raceOfCompetitorContext;
    
    private final GPSFixTrack<Competitor, GPSFixMoving> gpsFixTrack;
    
    public GPSFixTrackWithContext(HasRaceOfCompetitorContext raceOfCompetitorContext, GPSFixTrack<Competitor, GPSFixMoving> gpsFixTrack) {
        this.raceOfCompetitorContext = raceOfCompetitorContext;
        this.gpsFixTrack = gpsFixTrack;
    }
    
    @Override
    public HasRaceOfCompetitorContext getRaceOfCompetitorContext() {
        return raceOfCompetitorContext;
    }

    @Override
    public GPSFixTrack<Competitor, GPSFixMoving> getGPSFixTrack() {
        return gpsFixTrack;
    }
}
