package com.sap.sailing.domain.base.racegroup;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;

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

}
