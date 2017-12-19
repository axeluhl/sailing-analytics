package com.sap.sailing.domain.base;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.pairinglist.PairingList; 

public class PairingListRaceLogAdapter implements PairingList<RaceColumn, Fleet, Competitor, Boat> {
    
    @Override
    public Iterable<Pair<Competitor, Boat>> getCompetitors(RaceColumn raceColumn, Fleet fleet) {
        // TODO add boat to return value (bug2822)
        List<Pair<Competitor, Boat>> result = new ArrayList<>();
        int boatIndex = 0;
        for (Competitor competitor : raceColumn.getCompetitorsRegisteredInRacelog(fleet)) {
            result.add(new Pair<Competitor, Boat>(competitor, 
                    new BoatImpl("Boat " + (boatIndex + 1), new BoatClassImpl("49er", true), "DE" + boatIndex)));
            boatIndex++;
        }
        return result;
    }
    
}
