package com.sap.sailing.xrr.resultimport.impl;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionForCompetitorInRace;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionsForRace;
import com.sap.sailing.xrr.resultimport.Parser;
import com.sap.sailing.xrr.schema.Boat;
import com.sap.sailing.xrr.schema.Division;
import com.sap.sailing.xrr.schema.RaceResult;
import com.sap.sailing.xrr.schema.Team;


public class ScoreCorrectionForRaceResult implements ScoreCorrectionsForRace {
    private final Parser parser;
    private final Division division;
    private final Set<String> raceIDs;
    private final BigInteger raceNumber;
    
    private static final Logger logger = Logger.getLogger(ScoreCorrectionForRaceResult.class.getName());

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
                    Team team = parser.getTeam(raceResult.getTeamID());
                    if (team != null) {
                        Boat boat = parser.getBoat(team.getBoatID());
                        if (boat != null) {
                            result.add(boat.getSailNumber());
                        } else {
                            logger.severe("Could not find a boat for ID " + team.getBoatID() + " for team with ID " + 
                                    raceResult.getTeamID() + " for raceresult of race with ID "+ raceResult.getRaceID());
                        }
                    } else {
                        logger.severe("Could not find a team for ID " + raceResult.getTeamID() + " for raceresult of race with ID "+ 
                                raceResult.getRaceID());
                    }
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
