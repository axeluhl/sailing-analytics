package com.sap.sailing.xrr.resultimport.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionForCompetitorInRace;
import com.sap.sailing.xrr.resultimport.Parser;
import com.sap.sailing.xrr.schema.Crew;
import com.sap.sailing.xrr.schema.CrewPosition;
import com.sap.sailing.xrr.schema.Division;
import com.sap.sailing.xrr.schema.IFBoatStatus;
import com.sap.sailing.xrr.schema.Person;
import com.sap.sailing.xrr.schema.RaceResult;


public class RaceResultAsScoreCorrectionForCompetitorInRace implements ScoreCorrectionForCompetitorInRace {
    private final String sailID;
    private final Parser parser;
    private final RaceResult raceResult;
    
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
                    String teamSailID = parser.getBoat(parser.getTeam(raceResult.getTeamID()).getBoatID()).getSailNumber();
                    if (sailID.equals(teamSailID)) {
                        result = raceResult;
                        break;
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
        return raceResult.getRacePoints().doubleValue();
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
