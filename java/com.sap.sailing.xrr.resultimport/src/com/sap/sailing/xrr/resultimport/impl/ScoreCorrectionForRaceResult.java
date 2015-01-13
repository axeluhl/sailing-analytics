package com.sap.sailing.xrr.resultimport.impl;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionForCompetitorInRace;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionsForRace;
import com.sap.sailing.xrr.resultimport.Parser;
import com.sap.sailing.xrr.schema.Division;
import com.sap.sailing.xrr.schema.RaceResult;


public class ScoreCorrectionForRaceResult implements ScoreCorrectionsForRace {
    private final Parser parser;
    private final Division division;
    private final Set<String> raceIDs;
    private final BigInteger raceNumber;
    
    public ScoreCorrectionForRaceResult(Parser parser, Division division, BigInteger raceNumber, Set<String> raceIDs) {
        this.parser = parser;
        this.division = division;
        this.raceNumber = raceNumber;
        this.raceIDs = raceIDs;
    }

    @Override
    public String getRaceNameOrNumber() {
        return raceNumber.toString();
    }

    @Override
    public Set<String> getSailIDs() {
        Set<String> result = new HashSet<>();
        for (Object o : division.getSeriesResultOrRaceResultOrTRResult()) {
            // TODO what about TRRaceResult?
            if (o instanceof RaceResult) {
                RaceResult raceResult = (RaceResult) o;
                if (raceIDs.contains(raceResult.getRaceID())) {
                    result.add(parser.getBoat(parser.getTeam(raceResult.getTeamID()).getBoatID()).getSailNumber());
                }
            }
        }
        return result;
    }

    @Override
    public ScoreCorrectionForCompetitorInRace getScoreCorrectionForCompetitor(String sailID) {
        return new RaceResultAsScoreCorrectionForCompetitorInRace(parser, division, raceIDs, sailID);
    }

}
