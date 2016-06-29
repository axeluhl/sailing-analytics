package com.sap.sailing.gwt.home.server;

import java.util.Collection;
import java.util.TreeSet;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.home.communication.event.RaceListRaceDTO;
import com.sap.sailing.gwt.home.server.EventActionUtil.RaceCallback;

/**
 * {@link RaceCallback} implementation, which collects the set of {@link RaceContext#getFinishedRaceOrNull() finished
 * races} to show in the respective UI sections.
 */
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
    
    /**
     * @return the collected finished races
     */
    public Collection<RaceListRaceDTO> getResult() {
        return races;
    }

}
