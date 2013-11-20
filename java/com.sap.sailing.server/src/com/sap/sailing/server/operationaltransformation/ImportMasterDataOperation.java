package com.sap.sailing.server.operationaltransformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.LeaderboardMasterData;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.impl.MasterDataImportObjectCreationCountImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.domain.masterdataimport.EventMasterData;
import com.sap.sailing.domain.masterdataimport.FlexibleLeaderboardMasterData;
import com.sap.sailing.domain.masterdataimport.LeaderboardGroupMasterData;
import com.sap.sailing.domain.masterdataimport.RaceColumnMasterData;
import com.sap.sailing.domain.masterdataimport.RegattaLeaderboardMasterData;
import com.sap.sailing.domain.masterdataimport.RegattaMasterData;
import com.sap.sailing.domain.masterdataimport.ScoreCorrectionMasterData;
import com.sap.sailing.domain.masterdataimport.SeriesMasterData;
import com.sap.sailing.domain.masterdataimport.SingleScoreCorrectionMasterData;
import com.sap.sailing.domain.masterdataimport.WindTrackMasterData;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class ImportMasterDataOperation extends
        AbstractRacingEventServiceOperation<MasterDataImportObjectCreationCountImpl> {

    private static final long serialVersionUID = 3131715325307370303L;

    private static final Logger logger = Logger.getLogger(ImportMasterDataOperation.class.getName());

    private final LeaderboardGroupMasterData masterData;

    private final MasterDataImportObjectCreationCountImpl creationCount;

    private final DomainFactory baseDomainFactory;

    private final boolean override;

    public ImportMasterDataOperation(LeaderboardGroupMasterData masterData, boolean override, MasterDataImportObjectCreationCountImpl existingCreationCount, DomainFactory baseDomainFactory) {
        this.creationCount = new MasterDataImportObjectCreationCountImpl();
        this.creationCount.add(existingCreationCount);
        this.baseDomainFactory = baseDomainFactory;
        this.masterData = masterData;
        this.override = override;
    }

    @Override
    public MasterDataImportObjectCreationCountImpl internalApplyTo(RacingEventService toState) throws Exception {
        createLeaderboardGroupWithAllRelatedObjects(masterData, toState);
        return creationCount;
    }

    private void createLeaderboardGroupWithAllRelatedObjects(final LeaderboardGroupMasterData masterData,
            RacingEventService toState) {
        List<String> leaderboardNames = new ArrayList<String>();
        createCourseAreasAndEvents(masterData, toState);
        createRegattas(masterData, toState);
        Map<String, Leaderboard> existingLeaderboards = toState.getLeaderboards();
        for (final LeaderboardMasterData board : masterData.getLeaderboards()) {
            leaderboardNames.add(board.getName());
            if (existingLeaderboards.containsKey(board.getName())) {
                if (creationCount.alreadyAddedLeaderboardWithName(board.getName())) {
                    //Has already been added by this operation
                    continue;
                } else if (override) {
                    toState.removeLeaderboard(board.getName());
                    logger.info(String.format("Leaderboard with name %1$s already existed and has been overridden.",
                            board.getName()));
                } else {
                    logger.info(String.format("Leaderboard with name %1$s already exists and hasn't been overridden.",
                            board.getName()));
                    continue;
                }
            }
            setCourseAreaIfNecessary(board, toState);
            setRegattaIfNecessary(board, toState);
            Leaderboard leaderboard = board.getLeaderboard();
            if (leaderboard != null) {
                leaderboard.setDisplayName(board.getDisplayName());
                toState.addLeaderboard(leaderboard);
                creationCount.addOneLeaderboard(leaderboard.getName());
                Leaderboard newLeaderboard = toState.getLeaderboardByName(board.getName());
                addRaceColumnsIfNecessary(board, newLeaderboard, toState);
                // Set dummy tracked race, so that the leaderboard caches the competitors and
                // will accept the score corrections
                Pair<RaceColumn, Fleet> dummyColumnAndFleet = addDummyTrackedRace(board.getCompetitorsById().values(),
                        leaderboard, getRegattaIfPossible(leaderboard));
                boolean addedScoreCorrections = false;
                if (dummyColumnAndFleet.getA() != null && dummyColumnAndFleet.getB() != null) {
                    addScoreCorrectionsIfPossible(board.getScoreCorrection(), newLeaderboard);
                    addedScoreCorrections = true;
                }
                addCarriedPoints(leaderboard, board.getCarriedPoints(), board.getCompetitorsById());
                addSuppressedCompetitors(leaderboard, board.getSuppressedCompetitors(), new CompetitorByIdGetter() {
                    @Override
                    public Competitor getCompetitorById(String id) {
                        return board.getCompetitorsById().get(id);
                    }
                });
                addCompetitorDisplayNames(leaderboard, board.getDisplayNamesByCompetitorId(),
                        board.getCompetitorsById());
                addRaceLogEvents(leaderboard, board.getRaceLogEvents());
                toState.updateStoredLeaderboard(leaderboard);
                if (addedScoreCorrections) {
                    unsetDummy(dummyColumnAndFleet, leaderboard);
                }

            }
        }
        int[] overallLeaderboardDiscardThresholds = masterData.getOverallLeaderboardDiscardingRule() != null ? masterData.getOverallLeaderboardDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces() : null;
        ScoringSchemeType overallLeaderboardScoringSchemeType = masterData.getOverallLeaderboardScoringScheme() != null ? masterData.getOverallLeaderboardScoringScheme().getType() : null;
        final LeaderboardGroup leaderboardGroup;
        LeaderboardGroup existingLeaderboardGroup = toState.getLeaderboardGroupByName(masterData.getName());
        if (existingLeaderboardGroup != null && override) {
            logger.info(String.format("Leaderboard Group with name %1$s already existed and will be overridden.",
                    masterData.getName()));
            toState.removeLeaderboardGroup(masterData.getName());
            existingLeaderboardGroup = null;
        }
        if (existingLeaderboardGroup == null) {
            leaderboardGroup = toState.addLeaderboardGroup(masterData.getName(), masterData.getDescription(),
                    masterData.isDisplayGroupsRevese(), leaderboardNames, overallLeaderboardDiscardThresholds,
                    overallLeaderboardScoringSchemeType);
            creationCount.addOneLeaderboardGroup(masterData.getName());
        } else {
            leaderboardGroup = existingLeaderboardGroup;
            logger.info(String.format("Leaderboard Group with name %1$s already exists and hasn't been overridden.",
                    masterData.getName()));
        }
        if (masterData.hasOverallLeaderboard() && (override || existingLeaderboardGroup == null)) {
            if (existingLeaderboardGroup != null && existingLeaderboardGroup.getOverallLeaderboard() != null) {
                // remove old overall leaderboard if it existed
                toState.removeLeaderboard(existingLeaderboardGroup.getOverallLeaderboard().getName());
            }
            Leaderboard overallLeaderboard = leaderboardGroup.getOverallLeaderboard();
            addSuppressedCompetitors(overallLeaderboard, masterData.getOverallLeaderboardSuppressedCompetitorIds(),
                    new CompetitorByIdGetter() {
                        @Override
                        public Competitor getCompetitorById(String id) {
                            return masterData.getCompetitorById(id);
                        }
                    });
            Map<String, Double> factorsForMetaColumns = masterData.getMetaColumnsWithFactors();
            if (factorsForMetaColumns != null) {
                for (RaceColumn column : overallLeaderboard.getRaceColumns()) {
                    Double explicitFactor = factorsForMetaColumns.get(column.getName());
                    toState.updateLeaderboardColumnFactor(overallLeaderboard.getName(), column.getName(), explicitFactor);
                }
            }
            toState.getMongoObjectFactory().storeLeaderboardGroup(leaderboardGroup); // store changes to overall leaderboard
        }
    }

    private void addRaceLogEvents(Leaderboard leaderboard, Map<String, Map<String, List<RaceLogEvent>>> raceLogEvents) {
        for (Entry<String, Map<String, List<RaceLogEvent>>> entry : raceLogEvents.entrySet()) {
            String raceColumnName = entry.getKey();
            RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
            for (Entry<String, List<RaceLogEvent>> fleetEntry : entry.getValue().entrySet()) {
                String fleetName = fleetEntry.getKey();
                Fleet fleet = raceColumn.getFleetByName(fleetName);
                List<RaceLogEvent> logEvents = fleetEntry.getValue();
                RaceLog raceLog = raceColumn.getRaceLog(fleet);
                for (RaceLogEvent singleEvent : logEvents) {
                    raceLog.load(singleEvent);
                }
            }
        }
    }

    private void addCompetitorDisplayNames(Leaderboard leaderboard, Map<String, String> displayNamesByCompetitorId,
            Map<String, Competitor> competitorsById) {
        for (Entry<String, String> entry : displayNamesByCompetitorId.entrySet()) {
            leaderboard.setDisplayName(competitorsById.get(entry.getKey()), entry.getValue());
        }
    }
    
    interface CompetitorByIdGetter {
        Competitor getCompetitorById(String id);
    }

    public static void addSuppressedCompetitors(Leaderboard leaderboard, List<String> suppressedCompetitors,
            CompetitorByIdGetter competitorsById) {
        for (String id : suppressedCompetitors) {
            leaderboard.setSuppressed(competitorsById.getCompetitorById(id), true);
        }
    }

    private void addCarriedPoints(Leaderboard leaderboard, Map<String, Double> carriedPoints,
            Map<String, Competitor> competitorsById) {
        for (Entry<String, Double> entry : carriedPoints.entrySet()) {
            leaderboard.setCarriedPoints(competitorsById.get(entry.getKey()), entry.getValue());
        }
    }

    private Regatta getRegattaIfPossible(Leaderboard leaderboard) {
        if (leaderboard instanceof RegattaLeaderboard) {
            return ((RegattaLeaderboard) leaderboard).getRegatta();
        }
        return null;
    }

    private void unsetDummy(Pair<RaceColumn, Fleet> dummyColumnAndFleet, Leaderboard leaderboard) {
        RaceColumn raceColumn = dummyColumnAndFleet.getA();
        Fleet fleet = dummyColumnAndFleet.getB();
        raceColumn.setTrackedRace(fleet, null);
    }

    /**
     * Hack adding a dummy tracked race, so that the competitors will be added to the leaderboards
     * 
     * @param competitors
     * @param leaderboard
     * @return the race column and fleet the dummy was attached to
     */
    public Pair<RaceColumn, Fleet> addDummyTrackedRace(Iterable<Competitor> competitors, Leaderboard leaderboard,
            Regatta regatta) {
        RaceColumn raceColumn = null;
        Fleet fleet = null;
        Iterable<RaceColumn> raceColumns = leaderboard.getRaceColumns();
        Iterator<RaceColumn> raceColumnIterator = raceColumns.iterator();
        if (raceColumnIterator.hasNext()) {
            raceColumn = raceColumnIterator.next();
            Iterable<? extends Fleet> fleets = raceColumn.getFleets();
            Iterator<? extends Fleet> fleetIterator = fleets.iterator();
            if (fleetIterator.hasNext()) {
                fleet = fleetIterator.next();
                DummyTrackedRace dummy = new DummyTrackedRace(competitors, regatta, null);
                raceColumn.setTrackedRace(fleet, dummy);
            }
        }
        return new Pair<RaceColumn, Fleet>(raceColumn, fleet);
    }

    private void addScoreCorrectionsIfPossible(ScoreCorrectionMasterData scoreCorrectionMasterData,
            final Leaderboard leaderboard) {
        SettableScoreCorrection scoreCorrection = leaderboard.getScoreCorrection();
        scoreCorrection.setComment(scoreCorrectionMasterData.getComment());
        if (scoreCorrectionMasterData.getTimepointMillis() != null) {
            scoreCorrection.setTimePointOfLastCorrectionsValidity(new MillisecondsTimePoint(scoreCorrectionMasterData
                    .getTimepointMillis()));
        }
        for (Entry<String, Iterable<SingleScoreCorrectionMasterData>> scoreCorrectionEntry : scoreCorrectionMasterData
                .getCorrectionForRaceColumns().entrySet()) {
            String columnName = scoreCorrectionEntry.getKey();
            RaceColumn raceColumn = leaderboard.getRaceColumnByName(columnName);
            if (raceColumn != null) {
                for (SingleScoreCorrectionMasterData singleCorrection : scoreCorrectionEntry.getValue()) {
                    Competitor competitor = leaderboard.getCompetitorByIdAsString(singleCorrection.getCompetitorId());
                    scoreCorrection.setMaxPointsReason(competitor, raceColumn,
                            MaxPointsReason.valueOf(singleCorrection.getMaxPointsReason()));
                    if (singleCorrection.getExplicitScoreCorrection() != null) {
                        scoreCorrection.correctScore(competitor, raceColumn,
                                singleCorrection.getExplicitScoreCorrection());
                    }
                }
            }
        }

    }

    private void addRaceColumnsIfNecessary(LeaderboardMasterData board, Leaderboard newLeaderboard,
            RacingEventService toState) {
        if (board instanceof FlexibleLeaderboardMasterData) {
            for (RaceColumnMasterData raceColumnMasterData : ((FlexibleLeaderboardMasterData) board).getRaceColumns()) {
                RaceColumn raceColumn = toState.addColumnToLeaderboard(raceColumnMasterData.getName(), board.getName(),
                        raceColumnMasterData.isMedal());
                Set<WindTrackMasterData> windTrackMasterData = raceColumnMasterData.getWindTrackMasterData();
                if (windTrackMasterData != null) {
                    createWindTracks(windTrackMasterData, toState);
                }
                for (Map.Entry<String, RaceIdentifier> e : raceColumnMasterData.getRaceIdentifiersByFleetName()
                        .entrySet()) {
                    raceColumn.setRaceIdentifier(raceColumn.getFleetByName(e.getKey()), e.getValue());
                }
            }
        }
    }

    private void createWindTracks(Set<WindTrackMasterData> windTrackMasterData, RacingEventService toState) {
        for (WindTrackMasterData windMasterData : windTrackMasterData) {
            DummyTrackedRace trackedRaceWithNameAndId = new DummyTrackedRace(windMasterData.getRaceName(), windMasterData.getRaceId());
            WindTrack windTrack = toState.getWindStore().getWindTrack(windMasterData.getRegattaName(), trackedRaceWithNameAndId, windMasterData.getWindSource(), 0, -1);
            for (Wind fix : windMasterData.getFixes()) {
                windTrack.add(fix);
            }
        }
    }

    private void createRegattas(LeaderboardGroupMasterData masterData, RacingEventService toState) {
        Iterable<RegattaMasterData> regattaData = masterData.getRegattas();
        for (RegattaMasterData singleRegattaData : regattaData) {
            Regatta existingRegatta = toState.getRegatta(new RegattaName(singleRegattaData.getRegattaName()));
            if (existingRegatta != null) {
                if (creationCount.alreadyAddedRegattaWithId(existingRegatta.getId().toString())) {
                    //Already added earlier in this import process
                    continue;
                } else if (override) {
                    logger.info(String.format("Regatta with name %1$s already existed and has been overridden.",
                            singleRegattaData.getRegattaName()));
                    try {
                        toState.removeRegatta(existingRegatta);
                    } catch (IOException | InterruptedException e) {
                        logger.warning(String.format("Regatta with name %1$s could not be deleted due to an error.",
                                singleRegattaData.getRegattaName()));
                        e.printStackTrace();
                        continue;
                    }
                } else {
                    logger.info(String.format("Regatta with name %1$s already exists and hasn't been overridden.",
                            singleRegattaData.getRegattaName()));
                    continue;
                }
            }
            String id = singleRegattaData.getId();
            Iterable<Series> series = createSeries(singleRegattaData.getSeries(), toState);
            String baseName = singleRegattaData.getBaseName();
            String boatClassName = singleRegattaData.getBoatClassName();
            String defaultCourseAreaId = singleRegattaData.getDefaultCourseAreaId();
            String scoringSchemeType = singleRegattaData.getScoringSchemeType();
            boolean isPersistent = singleRegattaData.isPersistent();
            UUID courseAreaUUID = defaultCourseAreaId != null ? UUID.fromString(defaultCourseAreaId) : null;
            Regatta createdRegatta = toState.getOrCreateRegattaWithoutReplication(baseName, boatClassName,
                    UUID.fromString(id), series, isPersistent,
                    baseDomainFactory.createScoringScheme(ScoringSchemeType.valueOf(scoringSchemeType)),
                    courseAreaUUID).getA();
            toState.setPersistentRegattaForRaceIDs(createdRegatta, singleRegattaData.getRaceIdsAsStrings(), override);
            creationCount.addOneRegatta(createdRegatta.getId().toString());
        }

    }

    private Iterable<Series> createSeries(Iterable<SeriesMasterData> series, RacingEventService toState) {
        List<Series> result = new ArrayList<Series>();
        for (SeriesMasterData singleSeriesData : series) {
            String name = singleSeriesData.getName();
            boolean isMedal = singleSeriesData.isMedal();
            Iterable<Fleet> fleets = singleSeriesData.getFleets();
            Iterable<RaceColumnMasterData> raceColumnMasterData = singleSeriesData.getRaceColumnNames();
            List<String> raceColumnNames = new ArrayList<String>();
            for (RaceColumnMasterData rcmd : raceColumnMasterData) {
                raceColumnNames.add(rcmd.getName());
            }
            SeriesImpl newSeries = new SeriesImpl(name, isMedal, fleets, raceColumnNames, toState);
            Iterator<RaceColumnMasterData> raceColumnMasterDataIter = raceColumnMasterData.iterator();
            Iterator<? extends RaceColumnInSeries> raceColumnIter = newSeries.getRaceColumns().iterator();
            while (raceColumnMasterDataIter.hasNext()) {
                RaceColumnMasterData rcmd = raceColumnMasterDataIter.next();
                RaceColumnInSeries raceColumn = raceColumnIter.next();
                if (rcmd.getFactor() != null) {
                    raceColumn.setFactor(rcmd.getFactor());
                }
                for (Map.Entry<String, RaceIdentifier> e : rcmd.getRaceIdentifiersByFleetName().entrySet()) {
                    raceColumn.setRaceIdentifier(raceColumn.getFleetByName(e.getKey()), e.getValue());
                }
            }
            int[] resultDiscardingRule = singleSeriesData.getDiscardingRule();
            if (resultDiscardingRule != null) {
                newSeries.setResultDiscardingRule(new ThresholdBasedResultDiscardingRuleImpl(resultDiscardingRule));
            }
            result.add(newSeries);
        }
        return result;
    }

    private void createCourseAreasAndEvents(LeaderboardGroupMasterData masterData, RacingEventService toState) {
        Set<EventMasterData> events = masterData.getEvents();

        for (EventMasterData event : events) {
            String id = event.getId();
            Event existingEvent = toState.getEvent(UUID.fromString(id));
            if (existingEvent != null && override && !creationCount.alreadyAddedEventWithId(id)) {
                logger.info(String.format("Event with name %1$s already existed and will be overridden.",
                        event.getName()));
                toState.removeEvent(existingEvent.getId());
                existingEvent = null;
            }
            if (existingEvent == null) {
                String name = event.getName();
                String pubString = event.getPubUrl();
                String venueName = event.getVenueName();
                boolean isPublic = event.isPublic();
                Event newEvent = toState.createEventWithoutReplication(name, venueName, pubString, isPublic, UUID.fromString(id));
                creationCount.addOneEvent(newEvent.getId().toString());
            } else {
                logger.info(String.format("Event with name %1$s already exists and hasn't been overridden.",
                        event.getName()));
            }
            Iterable<Pair<String, String>> courseAreas = event.getCourseAreas();
            for (Pair<String, String> courseAreaEntry : courseAreas) {
                boolean alreadyExists = false;
                if (existingEvent != null
                        && existsInSet(existingEvent.getVenue().getCourseAreas(), courseAreaEntry.getA())) {
                    alreadyExists = true;
                }
                if (!alreadyExists) {
                    toState.addCourseAreaWithoutReplication(UUID.fromString(id), UUID.fromString(courseAreaEntry.getA()), courseAreaEntry.getB());
                } else {
                    logger.info(String
                            .format("Course area with id %1$s for event with id %2$s already exists and hasn't been overridden.",
                                    courseAreaEntry.getA(), id));
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
            if (flexBoard.getCourseAreaId() == null) {
                return;
            }
            CourseArea courseArea = toState.getCourseArea(UUID.fromString(flexBoard.getCourseAreaId()));
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
