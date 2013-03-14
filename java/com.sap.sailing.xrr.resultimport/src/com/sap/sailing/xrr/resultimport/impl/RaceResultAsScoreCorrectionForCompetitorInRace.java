package com.sap.sailing.xrr.resultimport.impl;

import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionForCompetitorInRace;
import com.sap.sailing.xrr.resultimport.Parser;
import com.sap.sailing.xrr.resultimport.schema.Crew;
import com.sap.sailing.xrr.resultimport.schema.Division;
import com.sap.sailing.xrr.resultimport.schema.Person;
import com.sap.sailing.xrr.resultimport.schema.RaceResult;

public class RaceResultAsScoreCorrectionForCompetitorInRace implements ScoreCorrectionForCompetitorInRace {
    private final String sailID;
    private final Parser parser;
    private final RaceResult raceResult;
    
    public RaceResultAsScoreCorrectionForCompetitorInRace(Parser parser, Division division, String raceID, String sailID) {
        this.sailID = sailID;
        this.parser = parser;
        this.raceResult = determineRaceResult(division, raceID, sailID, parser);
    }

    @Override
    public String getSailID() {
        return sailID;
    }

    private RaceResult determineRaceResult(Division division, String raceID, String sailID, Parser parser) {
        RaceResult result = null;
        for (Object o : division.getSeriesResultOrRaceResultOrTRRaceResult()) {
            // TODO what about TRRaceResult
            if (o instanceof RaceResult) {
                RaceResult raceResult = (RaceResult) o;
                if (raceID.equals(raceResult.getRaceID())) {
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
        for (Crew crew : parser.getTeam(raceResult.getTeamID()).getCrew()) {
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
        return MaxPointsReason.valueOf(raceResult.getScoreCode().name());
    }

    @Override
    public Boolean isDiscarded() {
        return raceResult.isDiscard();
    }

}
