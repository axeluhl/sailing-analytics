package com.sap.sailing.racecommittee.app.data.handlers;

import java.util.Collection;

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
    }

}
