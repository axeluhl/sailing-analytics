package com.sap.sailing.racecommittee.app.data.handlers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.Intent;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinderResult.ResolutionFailed;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.services.polling.RaceLogPollingService;

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
        return manager.getDataStore().getRaces();
    }

    @Override
    public void onResult(Collection<ManagedRace> data, boolean isCached) {
        if (!isCached) {
            Set<DeleteFromDataStore> deleteList = new HashSet<>();
            for (ManagedRace race : manager.getDataStore().getRaces()) {
                if (!data.contains(race)) {
                    deleteList.add(new DeleteFromDataStore(context, manager, race));
                }
            }
            for (DeleteFromDataStore action : deleteList) {
                action.run();
            }
            manager.addRaces(data);
            calcRaceState(data);
            manager.getDataStore().registerRaces(data);
        }
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

    private static class DeleteFromDataStore implements Runnable {

        private final Context context;
        private final OnlineDataManager manager;
        private final ManagedRace race;

        public DeleteFromDataStore(Context context, OnlineDataManager manager, ManagedRace race) {
            this.context = context;
            this.manager = manager;
            this.race = race;
        }

        @Override
        public void run() {
            manager.getDataStore().removeRace(race);

            Intent intent = new Intent(context, RaceLogPollingService.class);
            intent.setAction(AppConstants.INTENT_ACTION_POLLING_RACE_REMOVE);
            intent.putExtra(AppConstants.INTENT_ACTION_EXTRA, race.getId());
            context.startService(intent);
        }
    }
}
