package com.sap.sailing.racecommittee.app.data.handlers;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

import java.util.Collection;

public class CompetitorsDataHandler extends DataHandler<Collection<Competitor>> {
    
    private ManagedRace race;

    public CompetitorsDataHandler(OnlineDataManager manager, ManagedRace managedRace) {
        super(manager);
        race = managedRace;
    }
    
    @Override
    public boolean hasCachedResults() {
        return race.getCompetitors() != null;
    }
    
    @Override
    public Collection<Competitor> getCachedResults() {
        return race.getCompetitors();
    }

    @Override
    public void onResult(Collection<Competitor> data) {
        race.setCompetitors(data);
    }

}
