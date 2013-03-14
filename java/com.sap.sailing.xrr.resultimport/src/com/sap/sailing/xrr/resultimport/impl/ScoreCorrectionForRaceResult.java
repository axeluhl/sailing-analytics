package com.sap.sailing.xrr.resultimport.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionForCompetitorInRace;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionsForRace;
import com.sap.sailing.xrr.resultimport.Parser;
import com.sap.sailing.xrr.resultimport.schema.Division;
import com.sap.sailing.xrr.resultimport.schema.RaceResult;

public class ScoreCorrectionForRaceResult implements ScoreCorrectionsForRace {
    private final Parser parser;
    private final Division division;
    private final String raceID;
    
    public ScoreCorrectionForRaceResult(Parser parser, Division division, String raceID) {
        this.parser = parser;
        this.division = division;
        this.raceID = raceID;
    }

    @Override
    public String getRaceNameOrNumber() {
        return raceID;
    }

    @Override
    public Set<String> getSailIDs() {
        Set<String> result = new HashSet<>();
        for (Object o : division.getSeriesResultOrRaceResultOrTRRaceResult()) {
            // TODO what about TRRaceResult?
            if (o instanceof RaceResult) {
                RaceResult raceResult = (RaceResult) o;
                if (raceID.equals(raceResult.getRaceID())) {
                    result.add(parser.getBoat(parser.getTeam(raceResult.getTeamID()).getBoatID()).getSailNumber());
                }
            }
        }
        return result;
    }

    @Override
    public ScoreCorrectionForCompetitorInRace getScoreCorrectionForCompetitor(String sailID) {
        return new RaceResultAsScoreCorrectionForCompetitorInRace(parser, division, raceID, sailID);
    }

}
