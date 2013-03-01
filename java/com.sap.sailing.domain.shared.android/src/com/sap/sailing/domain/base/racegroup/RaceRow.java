package com.sap.sailing.domain.base.racegroup;

import com.sap.sailing.domain.base.Fleet;

/**
 * Set of {@link RaceCell} racing as given {@link Fleet}.
 */
public interface RaceRow {
    Fleet getFleet();

    Iterable<RaceCell> getCells();
}
