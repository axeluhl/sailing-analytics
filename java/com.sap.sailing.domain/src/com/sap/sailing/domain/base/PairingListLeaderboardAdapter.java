package com.sap.sailing.domain.base;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.pairinglist.PairingList; 

public class PairingListLeaderboardAdapter implements PairingList<RaceColumn, Fleet, Competitor, Boat> {
    
    @Override
    public Iterable<Pair<Competitor, Boat>> getCompetitors(RaceColumn raceColumn, Fleet fleet) {
        boolean isTrackedRace = (raceColumn.getTrackedRace(fleet) != null);
        List<Pair<Competitor, Boat>> result = new ArrayList<>();
        int boatIndex = 0;
        for (Competitor competitor : raceColumn.getAllCompetitors(fleet)) {
            Boat boat;
            if (isTrackedRace && raceColumn.getTrackedRace(fleet).getRace().getBoatOfCompetitorById(competitor) != null) {
                boat = raceColumn.getTrackedRace(fleet).getRace().getBoatOfCompetitorById(competitor);
            } else {
                // TODO fetch boats form leader board (bug4403)
                boat = new BoatImpl("Boat " + (boatIndex + 1), new BoatClassImpl("49er", true), "DE" + boatIndex);
            }
            result.add(new Pair<Competitor, Boat>(competitor, boat));
            boatIndex++;
        }
        return result;
    }
}
