package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasBravoTrackContext;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.BravoFixTrack;

public class BravoTrackWithContext implements HasBravoTrackContext {

    private final HasRaceOfCompetitorContext raceOfCompetitorContext;
    
    private final BravoFixTrack<Competitor> bravoFixTrack;
    
    public BravoTrackWithContext(HasRaceOfCompetitorContext raceOfCompetitorContext, BravoFixTrack<Competitor> bravoFixTrack) {
        this.raceOfCompetitorContext = raceOfCompetitorContext;
        this.bravoFixTrack = bravoFixTrack;
    }
    
    @Override
    public HasRaceOfCompetitorContext getRaceOfCompetitorContext() {
        return raceOfCompetitorContext;
    }

    @Override
    public BravoFixTrack<Competitor> getBravoFixTrack() {
        return bravoFixTrack;
    }

}
