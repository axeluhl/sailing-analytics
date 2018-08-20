package com.sap.sailing.domain.base.racegroup;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sse.common.Util;

/**
 * Describes the properties by which races may be filtered. This encompasses, in particular, the race's place
 * in the regatta structure 
 * @author Axel Uhl (d043530)
 *
 */
public interface FilterableRace {
    RaceGroup getRaceGroup();

    SeriesBase getSeries();
    
    Fleet getFleet();

    /**
     * @return the status of the race's state.
     */
    RaceLogRaceStatus getStatus();

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
    
    int getZeroBasedSeriesIndex();

    String getRaceColumnName();
}
