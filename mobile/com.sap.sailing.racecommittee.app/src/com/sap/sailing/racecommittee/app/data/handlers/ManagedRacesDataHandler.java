package com.sap.sailing.racecommittee.app.data.handlers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinderResult.ResolutionFailed;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public class ManagedRacesDataHandler extends DataHandler<Collection<ManagedRace>> {

    public ManagedRacesDataHandler(OnlineDataManager manager) {
        super(manager);
    }
    
    @Override
    public boolean hasCachedResults() {
        return !manager.getDataStore().getRaces().isEmpty();
    }
    
    @Override
    public Collection<ManagedRace> getCachedResults() {
        return manager.getDataStore().getRaces();
    }

    @Override
    public void onResult(Collection<ManagedRace> data) {
        manager.addRaces(data);
        calcRaceState(data);
    }

    private void calcRaceState(Collection<ManagedRace> data) {
        final Set<RaceState> raceStatesWithUnresolvedStartTimes = new HashSet<>();
        for (ManagedRace race : data) {
            race.calculateRaceState();
            if (race.getState() != null && race.getState().getStartTimeFinderResult().getResolutionFailed() == ResolutionFailed.RACE_LOG_UNRESOLVED) {
                // perhaps the race that this race's start time depends on hasn't been loaded yet; remember and try to resolve in the
                // next pass
                raceStatesWithUnresolvedStartTimes.add(race.getState());
            }
        }
        int oldNumberOfRaceStatesWithUnresolvedStartTimes;
        do {
            oldNumberOfRaceStatesWithUnresolvedStartTimes = raceStatesWithUnresolvedStartTimes.size();
            final Set<RaceState> resolved = new HashSet<>();
            for (final RaceState raceState : raceStatesWithUnresolvedStartTimes) {
                raceState.forceUpdate();
                if (raceState.getStartTimeFinderResult().getResolutionFailed() != ResolutionFailed.RACE_LOG_UNRESOLVED) {
                    resolved.add(raceState);
                }
            }
            raceStatesWithUnresolvedStartTimes.removeAll(resolved);
        } while (oldNumberOfRaceStatesWithUnresolvedStartTimes != raceStatesWithUnresolvedStartTimes.size());
    }
}
