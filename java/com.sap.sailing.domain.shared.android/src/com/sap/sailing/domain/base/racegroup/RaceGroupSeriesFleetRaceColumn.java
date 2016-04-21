package com.sap.sailing.domain.base.racegroup;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;

/**
 * Represents a quadruple of {@link RaceGroup} (representing a regatta or a flexible leaderboard), a {@link SeriesBase
 * series}, a {@link Fleet} and a race column, identified by its unique name within the leaderboard. An instance can be
 * constructed for a {@link FilterableRace} which then extracts these four properties from the race. Equal
 * objects of this type can only result for equal races because they identify a "race slot" in a regatta.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RaceGroupSeriesFleetRaceColumn extends RaceGroupSeriesFleet {
    private final String raceColumnName;

    public RaceGroupSeriesFleetRaceColumn(FilterableRace race) {
        super(race);
        raceColumnName = race.getRaceColumnName();
    }
    
    public RaceGroupSeriesFleetRaceColumn(RaceGroup raceGroup, SeriesBase series, Fleet fleet, String raceColumnName) {
        super(raceGroup, series, fleet);
        this.raceColumnName = raceColumnName;
    }

    public String getRaceColumnName() {
        return raceColumnName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((raceColumnName == null) ? 0 : raceColumnName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        RaceGroupSeriesFleetRaceColumn other = (RaceGroupSeriesFleetRaceColumn) obj;
        if (raceColumnName == null) {
            if (other.raceColumnName != null)
                return false;
        } else if (!raceColumnName.equals(other.raceColumnName))
            return false;
        return true;
    }
}
