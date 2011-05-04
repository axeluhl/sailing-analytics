package com.sap.sailing.domain.standings;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Timed;

public interface RaceStandingsSnapshot extends Timed {
	RaceDefinition getRace();
	Position getLastKnownPosition(Boat boat);
	boolean hasStarted(Boat boat);
	boolean hasFinished(Boat boat);
	Leg getLastKnownLeg(Boat boat);
}
