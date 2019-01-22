package com.sap.sailing.racecommittee.app.data.handlers;

import java.util.Map;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public class CompetitorsDataHandler extends DataHandler<Map<Competitor, Boat>> {

    private ManagedRace race;

    public CompetitorsDataHandler(OnlineDataManager manager, ManagedRace managedRace) {
        super(manager);
        race = managedRace;
    }

    @Override
    public boolean hasCachedResults() {
        return race.getCompetitors() != null && race.getCompetitors().size() != 0;
    }

    @Override
    public Map<Competitor, Boat> getCachedResults() {
        return race.getCompetitorsAndBoats();
    }

    @Override
    public void onResult(Map<Competitor, Boat> data, boolean isCached) {
        race.setCompetitors(data);
    }

}
