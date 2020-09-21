package com.sap.sailing.racecommittee.app.data.handlers;

import android.content.Context;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinderResult.ResolutionFailed;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ManagedRacesDataHandler extends DataHandler<Collection<ManagedRace>> {

    private Context context;

    public ManagedRacesDataHandler(Context context, OnlineDataManager manager) {
        super(manager);

        this.context = context;
    }

    @Override
    public boolean hasCachedResults() {
        return !manager.getDataStore().getRaces().isEmpty();
    }

    @Override
    public Collection<ManagedRace> getCachedResults() {
        return new ArrayList<>(manager.getDataStore().getRaces());
    }

    @Override
    public void onResult(Collection<ManagedRace> data, boolean isCached) {
        manager.getDataStore().clearRaces(context);
        manager.addRaces(data);
        calcRaceState(data);
        manager.getDataStore().registerRaces(context, data);
    }

    private void calcRaceState(Collection<ManagedRace> data) {
        final Set<RaceState> raceStatesWithUnresolvedStartTimes = new HashSet<>();
        for (ManagedRace race : data) {
            race.calculateRaceState();
            if (race.getState() != null && race.getState().getStartTimeFinderResult()
                    .getResolutionFailed() == ResolutionFailed.RACE_LOG_UNRESOLVED) {
                // perhaps the race that this race's start time depends on hasn't been loaded yet; remember and try to
                // resolve in the
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
                if (raceState.getStartTimeFinderResult()
                        .getResolutionFailed() != ResolutionFailed.RACE_LOG_UNRESOLVED) {
                    resolved.add(raceState);
                }
            }
            raceStatesWithUnresolvedStartTimes.removeAll(resolved);
        } while (oldNumberOfRaceStatesWithUnresolvedStartTimes != raceStatesWithUnresolvedStartTimes.size());
    }
}
