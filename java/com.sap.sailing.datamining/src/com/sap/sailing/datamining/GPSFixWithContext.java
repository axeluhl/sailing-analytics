package com.sap.sailing.datamining;

import com.sap.sailing.datamining.shared.Dimension;
import com.sap.sailing.datamining.shared.WindStrength;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface GPSFixWithContext extends GPSFixMoving {

    public LeaderboardGroup getLeaderboardGroup();
    public Leaderboard getLeaderboard();
    public CourseArea getCourseArea();
    public Fleet getFleet();
    public TrackedRace getTrackedRace();
    public BoatClass getBoatClass();
    public TrackedLeg getTrackedLeg();
    public int getLegNumber();
    /**
     * The leg type of the leg, which contains this gps fix. Can be <code>null</code> if there's no wind in the race.
     */
    public LegType getLegType();
    public Competitor getCompetitor();
    public Nationality getNationality();
    /**
     * The year of the start of the race.
     */
    public Integer getYear();
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
