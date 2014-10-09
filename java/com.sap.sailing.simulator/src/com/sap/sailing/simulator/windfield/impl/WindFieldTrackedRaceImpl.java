package com.sap.sailing.simulator.windfield.impl;

import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.simulator.TimedPosition;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;

/**
 * A windfield that can be used for race simulation and is based on the wind measurements & interpolation made available
 * as part of a {@link TrackedRace}. This is base data for visualizing race simulation on the {@link RaceSimulationOverlay}
 * of the {@link RaceMap}
 * 
 * @author Christopher Ronnewinkel (D036654)
 * 
 */
public class WindFieldTrackedRaceImpl extends WindFieldGeneratorImpl implements WindFieldGenerator {

    private static final long serialVersionUID = -7005970781594631010L;
    private TrackedRace race;
    
    public WindFieldTrackedRaceImpl(TrackedRace race) {
        super(null, null);
        this.race = race;
    }

    @Override
    public Wind getWind(TimedPosition timedPosition) {
        return this.race.getWind(timedPosition.getPosition(), timedPosition.getTimePoint());
    }
    
}
