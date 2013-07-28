package com.sap.sailing.racecommittee.app.data.handlers;

import java.util.Collection;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public class CompetitorsDataHandler extends DataHandler<Collection<Competitor>> {
    
    private ManagedRace race;

    public CompetitorsDataHandler(OnlineDataManager manager, LoadClient<Collection<Competitor>> client, ManagedRace managedRace) {
        super(manager);
        race = managedRace;
    }

    @Override
    public void onResult(Collection<Competitor> data) {
        //super.onSuccess(data);
        race.setCompetitors(data);
    }

}
