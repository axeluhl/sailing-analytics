package com.sap.sailing.ess40.resultimport.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionForCompetitorInRace;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionsForRace;
import com.sap.sse.common.Util;

public class ScoreCorrectionForRaceImpl implements ScoreCorrectionsForRace {
    private final int raceNumberStartingWithOne;
    private final Map<String, ScoreCorrectionForCompetitorInRace> scoreCorrectionsByTeamName;
    
    public ScoreCorrectionForRaceImpl(Map<String, List<Util.Pair<String, Double>>> actResults, int raceNumberStartingWithZero) {
        this.raceNumberStartingWithOne = raceNumberStartingWithZero+1;
        this.scoreCorrectionsByTeamName = new HashMap<String, ScoreCorrectionForCompetitorInRace>();
        for (Map.Entry<String, List<Util.Pair<String, Double>>> e : actResults.entrySet()) {
            String teamName = e.getKey();
            Util.Pair<String, Double> rankAndPoints;
            if (raceNumberStartingWithZero < e.getValue().size()) {
                rankAndPoints = e.getValue().get(raceNumberStartingWithZero);
            } else {
                rankAndPoints = null;
            }
            scoreCorrectionsByTeamName.put(teamName, new ScoreCorrectionForCompetitorInRaceImpl(teamName, rankAndPoints));
        }
    }

    @Override
    public String getRaceNameOrNumber() {
        return ""+raceNumberStartingWithOne;
    }

    @Override
    public Set<String> getSailIDs() {
        return scoreCorrectionsByTeamName.keySet();
    }

    @Override
    public ScoreCorrectionForCompetitorInRace getScoreCorrectionForCompetitor(String sailID) {
        return scoreCorrectionsByTeamName.get(sailID);
    }

}
