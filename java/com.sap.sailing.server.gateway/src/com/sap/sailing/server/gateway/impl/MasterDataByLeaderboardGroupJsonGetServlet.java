package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;

public class MasterDataByLeaderboardGroupJsonGetServlet extends AbstractJsonHttpServlet {

    private static final long serialVersionUID = 998103495657252850L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONParser jsonParser = new JSONParser();
        Map<String, LeaderboardGroup> leaderboardGroups = getService().getLeaderboardGroups();
        Set<String> requestedLeaderboardGroupNames = new HashSet<String>();
        try {
            JSONArray requestedLeaderboardGroupNamesJson = (JSONArray) jsonParser.parse(req.getReader());
            for (int i = 0; i < requestedLeaderboardGroupNamesJson.size(); i++) {
                String name = (String) requestedLeaderboardGroupNamesJson.get(i);
                requestedLeaderboardGroupNames.add(name);
            }
        } catch (ParseException e) {
            // No range supplied. Export all for now
            requestedLeaderboardGroupNames.addAll(leaderboardGroups.keySet());
        }

        JSONArray masterData = new JSONArray();

        for (String name : requestedLeaderboardGroupNames) {
            LeaderboardGroup leaderboardGroup = leaderboardGroups.get(name);
            masterData.add(createJsonWithMasterDataForLeaderboardGroup(leaderboardGroup));
        }

        setJsonResponseHeader(resp);
        masterData.writeJSONString(resp.getWriter());
    }

    private JSONObject createJsonWithMasterDataForLeaderboardGroup(LeaderboardGroup leaderboardGroup) {
        JSONObject jsonLeaderboardGroup = new JSONObject();
        jsonLeaderboardGroup.put("name", leaderboardGroup.getName());
        jsonLeaderboardGroup.put("description", leaderboardGroup.getDescription());
        jsonLeaderboardGroup.put("overallLeaderboard",
                createJsonForLeaderboard(leaderboardGroup.getOverallLeaderboard()));
        jsonLeaderboardGroup.put("leaderboards", createJsonArrayForLeaderboards(leaderboardGroup.getLeaderboards()));

        return jsonLeaderboardGroup;
    }

    private JSONArray createJsonArrayForLeaderboards(Iterable<Leaderboard> leaderboards) {
        JSONArray jsonLeaderBoards = new JSONArray();
        for (Leaderboard leaderboard : leaderboards) {
            JSONObject jsonLeaderboard = createJsonForLeaderboard(leaderboard);
            jsonLeaderBoards.add(jsonLeaderboard);
        }
        return jsonLeaderBoards;
    }

    private JSONObject createJsonForLeaderboard(Leaderboard leaderboard) {
        if (leaderboard == null) {
            return null;
        }
        JSONObject jsonLeaderboard = new JSONObject();
        jsonLeaderboard.put("name", leaderboard.getName());
        jsonLeaderboard.put("scoreCorrection", createJsonForScoreCorrection(leaderboard));
        jsonLeaderboard.put("competitors", createJsonArrayForCompetitors(leaderboard.getAllCompetitors()));
        jsonLeaderboard.put("raceColumns", createJsonArrayForRaceColumns(leaderboard.getRaceColumns()));

        return jsonLeaderboard;
    }

    private JSONArray createJsonArrayForRaceColumns(Iterable<RaceColumn> raceColumns) {
        JSONArray jsonRaceColumns = new JSONArray();
        for (RaceColumn raceColumn : raceColumns) {
            JSONObject jsonRaceColumn = createJsonForRaceColumn(raceColumn);
            jsonRaceColumns.add(jsonRaceColumn);
        }
        return jsonRaceColumns;
    }

    private JSONObject createJsonForRaceColumn(RaceColumn raceColumn) {
        JSONObject jsonRaceColumn = new JSONObject();
        jsonRaceColumn.put("name", raceColumn.getName());
        jsonRaceColumn.put("medalRace", raceColumn.isMedalRace());
        jsonRaceColumn.put("fleets", createJsonArrayForFleets(raceColumn.getFleets()));
        return jsonRaceColumn;
    }

    private Object createJsonArrayForFleets(Iterable<? extends Fleet> fleets) {
        JSONArray jsonFleets = new JSONArray();
        for (Fleet fleet : fleets) {
            JSONObject jsonFleet = createJsonForFleet(fleet);
            jsonFleets.add(jsonFleet);
        }
        return jsonFleets;
    }

    private JSONObject createJsonForFleet(Fleet fleet) {
        JSONObject jsonFleet = new JSONObject();
        jsonFleet.put("name", fleet.getName());
        jsonFleet.put("color", createJsonForColor(fleet.getColor()));
        jsonFleet.put("ordering", fleet.getOrdering());
        return jsonFleet;
    }

    private JSONObject createJsonForColor(Color color) {
        JSONObject jsonColor = new JSONObject();
        Triple<Integer, Integer, Integer> rgb = color.getAsRGB();
        jsonColor.put("r", rgb.getA());
        jsonColor.put("g", rgb.getB());
        jsonColor.put("b", rgb.getC());
        
        return jsonColor;
    }

    private JSONArray createJsonArrayForCompetitors(Iterable<Competitor> allCompetitors) {
        JSONArray jsonCompetitors = new JSONArray();
        for (Competitor competitor : allCompetitors) {
            JSONObject jsonCompetitor = createJsonForCompetitor(competitor);
            jsonCompetitors.add(jsonCompetitor);
        }
        return jsonCompetitors;
    }

    private JSONObject createJsonForCompetitor(Competitor competitor) {
        JSONObject jsonCompetitor = new JSONObject();
        jsonCompetitor.put("name", competitor.getName());
        jsonCompetitor.put("id", competitor.getId().toString());
        jsonCompetitor.put("boat", createJsonForBoat(competitor.getBoat()));
        jsonCompetitor.put("team", createJsonForTeam(competitor.getTeam()));
        return jsonCompetitor;
    }

    private JSONObject createJsonForTeam(Team team) {
        JSONObject jsonTeam = new JSONObject();
        jsonTeam.put("name", team.getName());
        jsonTeam.put("coach", createJsonForPerson(team.getCoach()));
        jsonTeam.put("sailors", createJsonArrayForPersons(team.getSailors()));
        jsonTeam.put("nationality", createJsonForNationality(team.getNationality()));
        return jsonTeam;
    }

    private JSONArray createJsonArrayForPersons(Iterable<? extends Person> persons) {
        JSONArray jsonPersons = new JSONArray();
        for (Person person : persons) {
            JSONObject jsonPerson = createJsonForPerson(person);
            jsonPersons.add(jsonPerson);
        }
        return jsonPersons;
    }

    private JSONObject createJsonForPerson(Person person) {
        if (person == null) {
            return null;
        }
        JSONObject jsonPerson = new JSONObject();
        jsonPerson.put("name", person.getName());
        Date dateOfBirth = person.getDateOfBirth();
        jsonPerson.put("dateOfBirth", dateOfBirth != null ? dateOfBirth.getTime() : null);
        jsonPerson.put("description", person.getDescription());
        jsonPerson.put("nationality", createJsonForNationality(person.getNationality()));
        return jsonPerson;
    }

    private JSONObject createJsonForNationality(Nationality nationality) {
        JSONObject jsonNationality = new JSONObject();
        jsonNationality.put("threeLetterIOCAcronym", nationality.getThreeLetterIOCAcronym());
        return jsonNationality;
    }

    private JSONObject createJsonForBoat(Boat boat) {
        JSONObject jsonBoat = new JSONObject();
        jsonBoat.put("name", boat.getName());
        jsonBoat.put("sailID", boat.getSailID());
        jsonBoat.put("boatClass", createJsonForBoatClass(boat.getBoatClass()));
        return jsonBoat;
    }

    private JSONObject createJsonForBoatClass(BoatClass boatClass) {
        JSONObject jsonBoatClass = new JSONObject();
        jsonBoatClass.put("name", boatClass.getName());
        return jsonBoatClass;
    }

    private JSONObject createJsonForScoreCorrection(Leaderboard leaderboard) {
        SettableScoreCorrection correction = leaderboard.getScoreCorrection();
        JSONObject jsonScoreCorrection = new JSONObject();
        jsonScoreCorrection.put("comment", correction.getComment());
        jsonScoreCorrection.put("timePoint", correction.getTimePointOfLastCorrectionsValidity());
        jsonScoreCorrection.put("forRaceColumns",
                createJSONArrayForScoreCorrectionsForRaceColumns(correction, leaderboard));

        return jsonScoreCorrection;
    }

    private JSONArray createJSONArrayForScoreCorrectionsForRaceColumns(SettableScoreCorrection correction,
            Leaderboard leaderboard) {
        JSONArray scoreCorrectionsForRaceColumns = new JSONArray();
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            if (correction.hasCorrectionFor(raceColumn)) {
                JSONObject scoreCorrectionsForRaceColumn = new JSONObject();
                scoreCorrectionsForRaceColumn.put("raceColumnName", raceColumn.getName());
                scoreCorrectionsForRaceColumn.put("forCompetitors",
                        createJsonForScoreCorrectionsForRaceColumnAndCompetitors(correction, raceColumn, leaderboard));
                scoreCorrectionsForRaceColumns.add(scoreCorrectionsForRaceColumn);
            }

        }

        return scoreCorrectionsForRaceColumns;
    }

    private JSONArray createJsonForScoreCorrectionsForRaceColumnAndCompetitors(SettableScoreCorrection correction,
            RaceColumn raceColumn, Leaderboard leaderboard) {
        JSONArray scoreCorrectionsForCompetitors = new JSONArray();
        for (Competitor competitor : leaderboard.getAllCompetitors()) {
            JSONObject scoreCorrectionForCompetitor = new JSONObject();
            scoreCorrectionForCompetitor.put("explicitScoreCorrection",
                    correction.getExplicitScoreCorrection(competitor, raceColumn));
            scoreCorrectionForCompetitor.put("maxPointsReason",
                    correction.getMaxPointsReason(competitor, raceColumn));
            scoreCorrectionsForCompetitors.add(scoreCorrectionForCompetitor);
        }

        return scoreCorrectionsForCompetitors;
    }

}
