package com.sap.sailing.domain.base.racegroup;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.common.TargetTimeInfo;
import com.sap.sse.common.Named;
import com.sap.sse.common.Util;

/**
 * A "race". Its {@link #getName() name} represents the Race Column name coming from the leaderboard / {@link RaceGroup}.
 * <p>
 * 
 * Because this and all other {@link RaceGroup} interfaces are used for communication with the Android applications a
 * {@link RaceCell} carries its {@link RaceLog} for easy serialization and transmission of race information.
 */
public interface RaceCell extends Named {
    RaceLog getRaceLog();

    double getFactor();

    Double getExplicitFactor();
    
    /**
     * A Series offers a sequence of RaceColumns, each of them split according to the Fleets modeled for the Series. A
     * {@link RaceCell} describes a "slot" in this grid, defined by the series, the fleet and the race column. While
     * this object's {@link #getName() name} represents the race column's name, this doesn't tell anything about the
     * "horizontal" position in the "grid" or in other words what the index is of the race column in which this cell
     * lies.
     * <p>
     * 
     * Indices returned by this method start with zero, meaning the first race column in the series. This corresponds to
     * what one would get by asking {@link Util#indexOf(Iterable, Object) Util.indexOf(series.getRaceColumns(),
     * thisCellsRaceColumn)}, except in case the first race column is a "virtual" one that holds a non-discardable
     * carry-forward result. In this case, the second Race Column, which is the first "non-virtual" one, receives
     * index 0.
     */
    int getZeroBasedIndexInFleet();

    /**
     * If there is a tracked race for this race and we have sufficient information about wind and the boat class's
     * polar / VPP data this method will return an estimated duration for how long the race will take based on the
     * current course.
     */
    TargetTimeInfo getTargetTime();
}
