package com.sap.sailing.racecommittee.app.domain.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.base.racegroup.RaceGroupSeries;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sse.common.Util;

/**
 * Represents a triple of {@link RaceGroup} (representing a regatta or a flexible leaderboard), a {@link SeriesBase series}
 * and a {@link Fleet}. An instance can be constructed for a {@link ManagedRace} which then extracts these three properties
 * from the race. Note that equal objects of this type can result for different races as long as they are in the equal
 * {@link RaceGroup}, {@link SeriesBase series} and {@link Fleet}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RaceGroupSeriesFleet extends RaceGroupSeries {

    private Fleet fleet;
    private int fleetOrder;

    public RaceGroupSeriesFleet(ManagedRace race) {
        super(race);
        fleet = race.getFleet();
        fleetOrder = getFleetIndex(race.getSeries().getFleets(), race.getFleet());
    }

    private int getFleetIndex(Iterable<? extends Fleet> fleets, Fleet fleet) {
        return Util.indexOf(fleets, fleet);
    }

    public Fleet getFleet() {
        return fleet;
    }

    public String getFleetName() {
        return fleet.getName();
    }

    public String getDisplayName() {
        return getDisplayName(false);
    }

    public String getDisplayName(boolean useDisplayName) {
        String name = super.getDisplayName(useDisplayName);
        if (fleet != null && !fleet.getName().equals(LeaderboardNameConstants.DEFAULT_FLEET_NAME)) {
            name += " - " + fleet.getName();
        }
        return name;
    }

    public int getFleetOrder() {
        return fleetOrder;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fleet == null) ? 0 : fleet.hashCode());
        result = prime * result + fleetOrder;
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
        RaceGroupSeriesFleet other = (RaceGroupSeriesFleet) obj;
        if (fleet == null) {
            if (other.fleet != null)
                return false;
        } else if (!fleet.equals(other.fleet))
            return false;
        if (fleetOrder != other.fleetOrder)
            return false;
        return true;
    }

}
