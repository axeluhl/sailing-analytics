package com.sap.sailing.server.operationaltransformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.LeaderboardMasterData;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.EventMasterData;
import com.sap.sailing.domain.base.impl.FlexibleLeaderboardMasterData;
import com.sap.sailing.domain.base.impl.LeaderboardGroupMasterData;
import com.sap.sailing.domain.base.impl.RegattaLeaderboardMasterData;
import com.sap.sailing.domain.base.impl.RegattaMasterData;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.base.impl.SeriesMasterData;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class ImportMasterDataOperation extends AbstractRacingEventServiceOperation<CreationCount> {
    
    private static final long serialVersionUID = 3131715325307370303L;

    private LeaderboardGroupMasterData masterData;
    
    private CreationCount creationCount = new CreationCount();
    
    private DomainFactory domainFactory = DomainFactory.INSTANCE;

    public ImportMasterDataOperation(LeaderboardGroupMasterData masterData) {
        this.masterData = masterData;
    }

    @Override
    public CreationCount internalApplyTo(RacingEventService toState) throws Exception {
        createLeaderboardGroupWithAllRelatedObjects(masterData, toState);
        return creationCount;
    }
    
    private void createLeaderboardGroupWithAllRelatedObjects(LeaderboardGroupMasterData masterData, RacingEventService toState) {
        List<String> leaderboardNames = new ArrayList<String>();
        Map<String, Leaderboard> existingLeaderboards = toState.getLeaderboards();
        createCourseAreasAndEvents(masterData, toState);
        createRegattas(masterData, toState);
        for (LeaderboardMasterData board : masterData.getLeaderboards()) {
            if (existingLeaderboards.containsKey(board.getName())) {
                // Leaderboard exists
                continue;
            }
            setCourseAreaIfNecessary(board, toState);
            setRegattaIfNecessary(board, toState);
            Leaderboard leaderboard = board.getLeaderboard();
            if (leaderboard != null) {
                leaderboard.setDisplayName(board.getDisplayName());
                toState.addLeaderboard(leaderboard);
                leaderboardNames.add(board.getName());
                creationCount.addOneLeaderboard();
                Leaderboard newLeaderboard = toState.getLeaderboardByName(board.getName());
                addRaceColumnsIfNecessary(board, newLeaderboard, toState);
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
        if (toState.getLeaderboardGroupByName(masterData.getName()) == null) {
            toState.addLeaderboardGroup(masterData.getName(), masterData.getDescription(),
                    masterData.isDisplayGroupsRevese(), leaderboardNames, overallLeaderboardDiscardThresholds,
                    overallLeaderboardScoringSchemeType);
            creationCount.addOneLeaderboardGroup();
        }
    }

    private void addRaceColumnsIfNecessary(LeaderboardMasterData board, Leaderboard newLeaderboard, RacingEventService toState) {
        if (board instanceof FlexibleLeaderboardMasterData) {
            for (Pair<String, Boolean> raceColumn : ((FlexibleLeaderboardMasterData) board).getRaceColumns()) {
                toState.addColumnToLeaderboard(raceColumn.getA(), board.getName(), raceColumn.getB());
            }
        }
    }

    private void createRegattas(LeaderboardGroupMasterData masterData, RacingEventService toState) {
        Iterable<RegattaMasterData> regattaData = masterData.getRegattas();
        for (RegattaMasterData singleRegattaData : regattaData) {
            if (toState.getRegatta(new RegattaName(singleRegattaData.getRegattaName())) != null) {
                continue;
            }
            String id = singleRegattaData.getId();
            Iterable<Series> series = createSeries(singleRegattaData.getSeries(), toState);
            String baseName = singleRegattaData.getBaseName();
            String boatClassName = singleRegattaData.getBoatClassName();
            String defaultCourseAreaId = singleRegattaData.getDefaultCourseAreaId();
            String scoringSchemeType = singleRegattaData.getScoringSchemeType();
            boolean isPersistent = singleRegattaData.isPersistent();
            toState.createRegatta(baseName, boatClassName, UUID.fromString(id), series, isPersistent,
                    domainFactory.createScoringScheme(ScoringSchemeType.valueOf(scoringSchemeType)),
                    UUID.fromString(defaultCourseAreaId));
            creationCount.addOneRegatta();
        }

    }

    private Iterable<Series> createSeries(Iterable<SeriesMasterData> series, RacingEventService toState) {
        List<Series> result = new ArrayList<Series>();
        for (SeriesMasterData singleSeriesData : series) {
            String name = singleSeriesData.getName();
            boolean isMedal = singleSeriesData.isMedal();
            Iterable<Fleet> fleets = singleSeriesData.getFleets();
            Iterable<String> raceColumnNames = singleSeriesData.getRaceColumnNames();
            result.add(new SeriesImpl(name, isMedal, fleets, raceColumnNames, toState));
        }
        return result;
    }

    private void createCourseAreasAndEvents(LeaderboardGroupMasterData masterData, RacingEventService toState) {
        Set<EventMasterData> events = masterData.getEvents();

        for (EventMasterData event : events) {
            String id = event.getId();
            Event existingEvent = toState.getEvent(UUID.fromString(id));
            if (existingEvent == null) {
                String name = event.getName();
                String pubString = event.getPubUrl();
                String venueName = event.getVenueName();
                boolean isPublic = event.isPublic();
                toState.addEvent(name, venueName, pubString, isPublic, UUID.fromString(id), new ArrayList<String>());
                creationCount.addOneEvent();
            }
            Iterable<Pair<String, String>> courseAreas = event.getCourseAreas();
            for (Pair<String, String> courseAreaEntry : courseAreas) {
                boolean alreadyExists = false;
                if (existingEvent != null
                        && existsInSet(existingEvent.getVenue().getCourseAreas(), courseAreaEntry.getA())) {
                    alreadyExists = true;
                }
                if (!alreadyExists) {
                    toState.addCourseArea(UUID.fromString(id), courseAreaEntry.getB(), courseAreaEntry.getA());
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

    private void setRegattaIfNecessary(LeaderboardMasterData board, RacingEventService toState) {
        if (board instanceof RegattaLeaderboardMasterData) {
            RegattaLeaderboardMasterData regattaBoard = (RegattaLeaderboardMasterData) board;
            Regatta regatta = toState.getRegatta(regattaBoard.getRegattaName());
            regattaBoard.setRegatta(regatta);
        }
    }

    private void setCourseAreaIfNecessary(LeaderboardMasterData board, RacingEventService toState) {
        if (board instanceof FlexibleLeaderboardMasterData) {
            FlexibleLeaderboardMasterData flexBoard = (FlexibleLeaderboardMasterData) board;
            CourseArea courseArea = toState.getCourseArea(flexBoard.getCourseAreaId());
            flexBoard.setCourseArea(courseArea);
        }
    }


    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

}
