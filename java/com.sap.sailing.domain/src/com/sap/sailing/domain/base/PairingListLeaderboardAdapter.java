package com.sap.sailing.domain.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.pairinglist.PairingList; 

public class PairingListLeaderboardAdapter implements PairingList<RaceColumn, Fleet, Competitor, Boat> {
    
    @Override
    public Iterable<Pair<Competitor, Boat>> getCompetitors(RaceColumn raceColumn, Fleet fleet) {
        List<Pair<Competitor, Boat>> result = new ArrayList<>();
        for (Entry<Competitor, Boat> competitorAndBoat : raceColumn.getAllCompetitorsAndTheirBoats(fleet).entrySet()) {
            result.add(new Pair<Competitor, Boat>(competitorAndBoat.getKey(), competitorAndBoat.getValue()));
        }
        return result;
    }
    
}
