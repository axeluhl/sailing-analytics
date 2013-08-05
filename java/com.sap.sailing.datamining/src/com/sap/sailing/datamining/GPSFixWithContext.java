package com.sap.sailing.datamining;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;

public interface GPSFixWithContext extends GPSFixMoving {
    
    public Competitor getCompetitor();
    /**
     * The leg type of the leg, which contains this gps fix. Can be <code>null</code> if there's no wind in the race.
     */
    public LegType getLegType();
    public int getLegNumber();
    public TrackedRace getTrackedRace();
    public Regatta getRegatta();
    /**
     * The wind at the position and the time of this gps fix. Can be <code>null</code> if there's no wind in the race.
     */
    public Wind getWind();

}
