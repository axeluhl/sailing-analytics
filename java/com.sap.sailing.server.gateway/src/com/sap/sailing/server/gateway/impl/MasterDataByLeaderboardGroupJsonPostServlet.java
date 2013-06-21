package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.LeaderboardMasterData;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.base.impl.EventMasterData;
import com.sap.sailing.domain.base.impl.FlexibleLeaderboardMasterData;
import com.sap.sailing.domain.base.impl.RegattaLeaderboardMasterData;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.BoatClassJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.NationalityJsonDeserialzer;
import com.sap.sailing.server.gateway.deserialization.impl.PersonJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.TeamJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.masterdata.impl.CompetitorMasterDataDeserializer;
import com.sap.sailing.server.gateway.deserialization.masterdata.impl.EventMasterDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.masterdata.impl.LeaderboardGroupMasterData;
import com.sap.sailing.server.gateway.deserialization.masterdata.impl.LeaderboardGroupMasterDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.masterdata.impl.LeaderboardMasterDataJsonDeserializer;

public class MasterDataByLeaderboardGroupJsonPostServlet extends AbstractJsonHttpServlet {

    private static final long serialVersionUID = 998103495657252850L;
    private DomainFactory domainFactory;

    private CreationCount creationCount;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        domainFactory = DomainFactory.INSTANCE;
        creationCount = new CreationCount();
        JsonDeserializer<BoatClass> boatClassDeserializer = new BoatClassJsonDeserializer(domainFactory);

        JsonDeserializer<Nationality> nationalityDeserializer = new NationalityJsonDeserialzer();
        JsonDeserializer<Person> personDeserializer = new PersonJsonDeserializer(nationalityDeserializer);
        JsonDeserializer<Team> teamDeserializer = new TeamJsonDeserializer(personDeserializer);
        JsonDeserializer<Competitor> competitorDeserializer = new CompetitorMasterDataDeserializer(
                boatClassDeserializer, teamDeserializer, domainFactory);
        JsonDeserializer<LeaderboardMasterData> leaderboardDeserializer = new LeaderboardMasterDataJsonDeserializer(
                competitorDeserializer, domainFactory);
        JsonDeserializer<EventMasterData> eventDeserializer = new EventMasterDataJsonDeserializer();
        JsonDeserializer<LeaderboardGroupMasterData> leaderboardGroupMasterDataDeserializer = new LeaderboardGroupMasterDataJsonDeserializer(
                leaderboardDeserializer, eventDeserializer);
        JSONParser parser = new JSONParser();
        try {
            JSONArray leaderboardGroupsMasterDataJsonArray = (JSONArray) parser.parse(new InputStreamReader(req
                    .getInputStream()));
            for (Object leaderBoardGroupMasterData : leaderboardGroupsMasterDataJsonArray) {
                JSONObject leaderBoardGroupMasterDataJson = (JSONObject) leaderBoardGroupMasterData;
                LeaderboardGroupMasterData masterData = leaderboardGroupMasterDataDeserializer
                        .deserialize(leaderBoardGroupMasterDataJson);
                createLeaderboardGroupWithAllRelatedObjects(masterData);
            }
        } catch (ParseException e) {
            resp.sendError(400);
            e.printStackTrace();
        }
        resp.getWriter().write(creationCount.toJson().toJSONString());
    }

    private void createLeaderboardGroupWithAllRelatedObjects(LeaderboardGroupMasterData masterData) {
        List<String> leaderboardNames = new ArrayList<String>();
        Map<String, Leaderboard> existingLeaderboards = getService().getLeaderboards();
        createCourseAreasAndEvents(masterData);
        for (LeaderboardMasterData board : masterData.getLeaderboards()) {
            if (existingLeaderboards.containsKey(board.getName())) {
                // Leaderboard exists
                continue;
            }
            setCourseAreaIfNecessary(board);
            setRegattaIfNecessary(board);
            Leaderboard leaderboard = board.getLeaderboard();
            if (leaderboard != null) {
                leaderboard.setDisplayName(board.getDisplayName());
                getService().addLeaderboard(leaderboard);
                leaderboardNames.add(board.getName());
                creationCount.addOneLeaderboard();
            }
        }
        int[] overallLeaderboardDiscardThresholds = null;
        ScoringSchemeType overallLeaderboardScoringSchemeType = null;
        LeaderboardMasterData overallLeaderboard = masterData.getOverallLeaderboardMasterData();
        if (overallLeaderboard != null && overallLeaderboard instanceof FlexibleLeaderboardMasterData) {
            FlexibleLeaderboardMasterData flex = (FlexibleLeaderboardMasterData) overallLeaderboard;
            overallLeaderboardDiscardThresholds = overallLeaderboard.getResultDiscardingRule()
                    .getDiscardIndexResultsStartingWithHowManyRaces();
            overallLeaderboardScoringSchemeType = flex.getScoringScheme().getType();
        }
        if (getService().getLeaderboardGroupByName(masterData.getName()) == null) {
            getService().addLeaderboardGroup(masterData.getName(), masterData.getDescription(),
                    masterData.isDisplayGroupsRevese(), leaderboardNames, overallLeaderboardDiscardThresholds,
                    overallLeaderboardScoringSchemeType);
            creationCount.addOneLeaderboardGroup();
        }
    }

    private void createCourseAreasAndEvents(LeaderboardGroupMasterData masterData) {
        Set<EventMasterData> events = masterData.getEvents();

        for (EventMasterData event : events) {
            String id = event.getId();
            Event existingEvent = getService().getEvent(id);
            if (existingEvent == null) {
                String name = event.getName();
                String pubString = event.getPubUrl();
                String venueName = event.getVenueName();
                boolean isPublic = event.isPublic();
                getService().addEvent(name, venueName, pubString, isPublic, id, new ArrayList<String>());
                creationCount.addOneEvent();
            }
            Map<String, String> courseAreas = event.getCourseAreas();
            for (Entry<String, String> courseAreaEntry : courseAreas.entrySet()) {
                boolean alreadyExists = false;
                if (existingEvent != null && existsInSet(existingEvent.getVenue().getCourseAreas(), courseAreaEntry.getKey())) {
                    alreadyExists = true;
                }
                if (!alreadyExists) {
                    getService().addCourseArea(id, courseAreaEntry.getValue(), courseAreaEntry.getKey());
                }
            }
        }
    }

    /**
     * 
     * @param iterable
     * @param key
     * @return true if course with given id exists in iterable
     */
    private boolean existsInSet(Iterable<CourseArea> iterable, String key) {
        for (CourseArea area : iterable) {
            if (area.getId().toString().matches(key)) {
                return true;
            }
        }
        return false;
    }

    private void setRegattaIfNecessary(LeaderboardMasterData board) {
        if (board instanceof RegattaLeaderboardMasterData) {
            RegattaLeaderboardMasterData regattaBoard = (RegattaLeaderboardMasterData) board;
            Regatta regatta = getService().getRegatta(regattaBoard.getRegattaName());
            regattaBoard.setRegatta(regatta);
        }
    }

    private void setCourseAreaIfNecessary(LeaderboardMasterData board) {
        if (board instanceof FlexibleLeaderboardMasterData) {
            FlexibleLeaderboardMasterData flexBoard = (FlexibleLeaderboardMasterData) board;
            CourseArea courseArea = getService().getCourseArea(flexBoard.getCourseAreaId());
            flexBoard.setCourseArea(courseArea);
        }
    }

}
