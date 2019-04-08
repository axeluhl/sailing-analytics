package com.sap.sailing.xrr.resultimport.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionForCompetitorInRace;
import com.sap.sailing.xrr.resultimport.Parser;
import com.sap.sailing.xrr.schema.Boat;
import com.sap.sailing.xrr.schema.Crew;
import com.sap.sailing.xrr.schema.CrewPosition;
import com.sap.sailing.xrr.schema.Division;
import com.sap.sailing.xrr.schema.IFBoatStatus;
import com.sap.sailing.xrr.schema.Person;
import com.sap.sailing.xrr.schema.RaceResult;
import com.sap.sailing.xrr.schema.Team;


public class RaceResultAsScoreCorrectionForCompetitorInRace implements ScoreCorrectionForCompetitorInRace {
    private final String sailID;
    private final Parser parser;
    private final RaceResult raceResult;
    
    private static final Logger logger = Logger.getLogger(RaceResultAsScoreCorrectionForCompetitorInRace.class.getName());

    public RaceResultAsScoreCorrectionForCompetitorInRace(Parser parser, Division division, Set<String> raceIDs, String sailID) {
        this.sailID = sailID;
        this.parser = parser;
        this.raceResult = determineRaceResult(division, raceIDs, sailID, parser);
    }

    @Override
    public String getSailID() {
        return sailID;
    }

    private RaceResult determineRaceResult(Division division, Set<String> raceIDs, String sailID, Parser parser) {
        RaceResult result = null;
        for (Object o : division.getSeriesResultOrRaceResultOrTRResult()) {
            // TODO what about TRResult
            if (o instanceof RaceResult) {
                RaceResult raceResult = (RaceResult) o;
                if (raceIDs.contains(raceResult.getRaceID())) {
                    Team team = parser.getTeam(raceResult.getTeamID());
                    if (team != null) {
                        Boat boat = parser.getBoat(team.getBoatID());
                        if (boat != null) {
                            String teamSailID = boat.getSailNumber();
                            if (sailID.equals(teamSailID)) {
                                result = raceResult;
                                break;
                            }
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
    public String getCompetitorName() {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        // sort skipper to front of list
        final List<Crew> sortedCrew = new ArrayList<>(parser.getTeam(raceResult.getTeamID()).getCrew());
        Collections.sort(sortedCrew, new Comparator<Crew>() {
            @Override
            public int compare(Crew o1, Crew o2) {
                if (o1.getPosition() == CrewPosition.S) {
                    return -1;
                } else if (o2.getPosition() == CrewPosition.S) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        for (Crew crew : sortedCrew) {
            if (first) {
                first = false;
            } else {
                result.append(" + ");
            }
            Person person = parser.getPerson(crew.getPersonID());
            result.append(person.getFamilyName());
            result.append(", ");
            result.append(person.getGivenName());
        }
        return result.toString();
    }

    @Override
    public Double getPoints() {
        final BigDecimal racePoints = raceResult.getRacePoints();
        return racePoints == null ? null : racePoints.doubleValue();
    }

    @Override
    public MaxPointsReason getMaxPointsReason() {
        final IFBoatStatus scoreCode = raceResult.getScoreCode();
        final MaxPointsReason result;
        if (scoreCode == null) {
            result = MaxPointsReason.NONE;
        } else {
            result = MaxPointsReason.valueOf(scoreCode.name());
        }
        return result;
    }

    @Override
    public Boolean isDiscarded() {
        return raceResult.isDiscard();
    }

}
