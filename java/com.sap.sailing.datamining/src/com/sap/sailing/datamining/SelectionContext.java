package com.sap.sailing.datamining;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;

public interface SelectionContext {
    
    public TrackedRegatta getTrackedRegatta();
    public TrackedRace getTrackedRace();
    public int getLegNumber();
    public Competitor getCompetitor();

}
