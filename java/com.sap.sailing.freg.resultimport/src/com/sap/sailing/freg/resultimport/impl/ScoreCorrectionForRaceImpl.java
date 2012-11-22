package com.sap.sailing.freg.resultimport.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionForCompetitorInRace;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionsForRace;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.freg.resultimport.CompetitorEntry;
import com.sap.sailing.freg.resultimport.CompetitorRow;
import com.sap.sailing.freg.resultimport.RegattaResults;

public class ScoreCorrectionForRaceImpl implements ScoreCorrectionsForRace {
    private final int raceNumberStartingWithOne;
    private final Map<String, ScoreCorrectionForCompetitorInRace> scoreCorrectionsBySailID;
    
    public ScoreCorrectionForRaceImpl(RegattaResults regattaResult, int raceNumberStartingWithZero) {
        this.raceNumberStartingWithOne = raceNumberStartingWithZero+1;
        this.scoreCorrectionsBySailID = new HashMap<String, ScoreCorrectionForCompetitorInRace>();
        for (CompetitorRow competitorRow : regattaResult.getCompetitorResults()) {
            String teamName = competitorRow.getTeamName();
            CompetitorEntry competitorEntry;
            if (raceNumberStartingWithZero < Util.size(competitorRow.getRankAndMaxPointsReasonAndPointsAndDiscarded())) {
                competitorEntry = Util.get(competitorRow.getRankAndMaxPointsReasonAndPointsAndDiscarded(), raceNumberStartingWithZero);
            } else {
                competitorEntry = null;
            }
            scoreCorrectionsBySailID.put(competitorRow.getSailID(),
                    new ScoreCorrectionForCompetitorInRaceImpl(competitorRow.getSailID(), teamName, competitorEntry));
        }
    }

    @Override
    public String getRaceNameOrNumber() {
        return ""+raceNumberStartingWithOne;
    }

    @Override
    public Set<String> getSailIDs() {
        return scoreCorrectionsBySailID.keySet();
    }

    @Override
    public ScoreCorrectionForCompetitorInRace getScoreCorrectionForCompetitor(String sailID) {
        return scoreCorrectionsBySailID.get(sailID);
    }

}
