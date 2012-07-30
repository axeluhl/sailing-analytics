package com.sap.sailing.winregatta.resultimport.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionForCompetitorInRace;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionsForRace;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.resultimport.CompetitorEntry;
import com.sap.sailing.resultimport.CompetitorRow;
import com.sap.sailing.resultimport.RegattaResults;

public class ScoreCorrectionForRaceImpl implements ScoreCorrectionsForRace {
    private final int raceNumber;
    private final Map<String, ScoreCorrectionForCompetitorInRace> scoreCorrectionsBySailID;
    
    public ScoreCorrectionForRaceImpl(RegattaResults regattaResult, int raceNumber) {
    	this.raceNumber = raceNumber;
        this.scoreCorrectionsBySailID = new HashMap<String, ScoreCorrectionForCompetitorInRace>();
        for (CompetitorRow competitorResult : regattaResult.getCompetitorResults()) {
            String teamName = competitorResult.getTeamName();
            CompetitorEntry competitorEntry;
            if (raceNumber < Util.size(competitorResult.getRankAndMaxPointsReasonAndPointsAndDiscarded())) {
                competitorEntry = Util.get(competitorResult.getRankAndMaxPointsReasonAndPointsAndDiscarded(), raceNumber);
            } else {
                competitorEntry = null;
            }
            scoreCorrectionsBySailID.put(competitorResult.getSailID(),
                    new ScoreCorrectionForCompetitorInRaceImpl(competitorResult.getSailID(), teamName, competitorEntry));
        }
    }

    @Override
    public String getRaceNameOrNumber() {
        return ""+raceNumber;
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
