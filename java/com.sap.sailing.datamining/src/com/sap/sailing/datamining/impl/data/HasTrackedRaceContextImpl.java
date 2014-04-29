package com.sap.sailing.datamining.impl.data;

import java.util.Calendar;

import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.data.HasTrackedRegattaContext;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.TrackedRace;

public class HasTrackedRaceContextImpl extends HasTrackedRegattaContextImpl implements HasTrackedRaceContext {

    private final Fleet fleet;
    private final TrackedRace trackedRace;
    private Integer year;
    private boolean yearHasBeenInitialized;
    
    public HasTrackedRaceContextImpl(HasTrackedRegattaContext regattaContext, Fleet fleet, TrackedRace trackedRace) {
        this(regattaContext.getEvent(), regattaContext.getRegatta(), fleet, trackedRace);
    }

    public HasTrackedRaceContextImpl(Event event, Regatta regatta, Fleet fleet, TrackedRace trackedRace) {
        super(event, regatta);
        this.fleet = fleet;
        this.trackedRace = trackedRace;
    }
    
    @Override
    public TrackedRace getTrackedRace() {
        return getTrackedRace();
    }

    @Override
    public Fleet getFleet() {
        return fleet;
    }
    
    @Override
    public RaceDefinition getRace() {
        return trackedRace.getRace();
    }

    @Override
    public Integer getYear() {
        if (!yearHasBeenInitialized) {
            initializeYear();
        }
        return year;
    }

    private void initializeYear() {
        TimePoint time = getTrackedRace().getStartOfRace() != null ? getTrackedRace().getStartOfRace() : getTrackedRace().getStartOfTracking();
        if (time == null) {
            year = 0;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time.asDate());
        year = calendar.get(Calendar.YEAR);
        yearHasBeenInitialized = true;
    }

}