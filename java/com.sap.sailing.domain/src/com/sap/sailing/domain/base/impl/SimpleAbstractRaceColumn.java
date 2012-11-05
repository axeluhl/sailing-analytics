package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.util.impl.RaceColumnListeners;

public abstract class SimpleAbstractRaceColumn implements RaceColumn {
    private static final long serialVersionUID = -3590156714385187908L;
    private final RaceColumnListeners raceColumnListeners;

    /**
     * The factor by which a medal race score is multiplied by default in the overall point scheme.
     * 
     * @see #getFactor()
     */
    private static final double DEFAULT_MEDAL_RACE_FACTOR = 2.0;

    
    public SimpleAbstractRaceColumn() {
        raceColumnListeners = new RaceColumnListeners();
    }
    
    @Override
    public Pair<Competitor, RaceColumn> getKey(Competitor competitor) {
        return new Pair<Competitor, RaceColumn>(competitor, this);
    }

    public RaceColumnListeners getRaceColumnListeners() {
        return raceColumnListeners;
    }

    @Override
    public void addRaceColumnListener(RaceColumnListener listener) {
        getRaceColumnListeners().addRaceColumnListener(listener);
    }

    @Override
    public void removeRaceColumnListener(RaceColumnListener listener) {
        getRaceColumnListeners().removeRaceColumnListener(listener);
    }

    @Override
    public RaceDefinition getRaceDefinition(Fleet fleet) {
        TrackedRace trackedRace = getTrackedRace(fleet);
        RaceDefinition result = null;
        if (trackedRace != null) {
            result = trackedRace.getRace();
        }
        return result;
    }

    @Override
    public double getFactor() {
        return isMedalRace() ? DEFAULT_MEDAL_RACE_FACTOR : 1.;
    }
}
