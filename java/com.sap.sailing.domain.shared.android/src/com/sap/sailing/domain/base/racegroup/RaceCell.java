package com.sap.sailing.domain.base.racegroup;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sse.common.Named;

/**
 * A "race".
 * <p>
 * 
 * Because this and all other {@link RaceGroup} interfaces are used for communication with the Android applications a
 * {@link RaceCell} carries its {@link RaceLog} for easy serialization and transmission of race information.
 */
public interface RaceCell extends Named {
    RaceLog getRaceLog();
}
