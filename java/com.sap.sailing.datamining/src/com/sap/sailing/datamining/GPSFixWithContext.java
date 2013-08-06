package com.sap.sailing.datamining;

import com.sap.sailing.datamining.shared.Dimension;
import com.sap.sailing.datamining.shared.WindStrength;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public interface GPSFixWithContext extends GPSFixMoving {
    
    public Competitor getCompetitor();
    public int getLegNumber();
    public RaceDefinition getRace();
    public Regatta getRegatta();
    public BoatClass getBoatClass();
    public Nationality getNationality();
    /**
     * The year of the start of the race.
     */
    public Integer getYear();
    /**
     * The leg type of the leg, which contains this gps fix. Can be <code>null</code> if there's no wind in the race.
     */
    public LegType getLegType();
    /**
     * The wind strength at the position and the time of this gps fix. Can be <code>null</code> if there's no wind in the race.
     */
    public WindStrength getWindStrength();
    
    /**
     * A string representation of the value of the given dimension of this GPS-Fix (for example the name of
     * the competitor of this fix, if the dimension is {@link Dimension#CompetitorName}). Can be <code>null</code>,
     * if the value is <code>null</code> (possible for wind or leg type).
     * 
     * @return A string representation of the value of the given dimension of this GPS-Fix.
     */
    public String getStringRepresentation(Dimension dimension);

}
