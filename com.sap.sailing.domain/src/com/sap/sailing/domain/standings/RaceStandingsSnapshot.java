package com.sap.sailing.domain.standings;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.base.Waypoint;

public interface RaceStandingsSnapshot extends Timed {
    RaceDefinition getRace();

    Position getLastKnownPosition(Boat boat);

    boolean hasStarted(Boat boat);

    boolean hasFinished(Boat boat);

    Leg getCurrentLeg(Boat boat);

    /**
     * @return 0 in case the boat didn't pass <code>mark</code> yet
     */
    int getRankAtMark(Boat boat, Waypoint mark);
}