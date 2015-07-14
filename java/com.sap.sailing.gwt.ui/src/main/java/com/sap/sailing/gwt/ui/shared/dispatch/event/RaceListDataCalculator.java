package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Collection;
import java.util.TreeSet;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.RaceCallback;

@GwtIncompatible
public class RaceListDataCalculator implements RaceCallback {
    private final TreeSet<RaceListRaceDTO> races = new TreeSet<>();

    @Override
    public void doForRace(RaceContext context) {
        RaceListRaceDTO finishedRace = context.getFinishedRaceOrNull();
        if(finishedRace != null) {
            races.add(finishedRace);
        }
    }
    
    public Collection<RaceListRaceDTO> getResult() {
        return races;
    }

}
