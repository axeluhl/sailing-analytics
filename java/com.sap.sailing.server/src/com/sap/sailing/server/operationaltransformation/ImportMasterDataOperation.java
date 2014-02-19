package com.sap.sailing.server.operationaltransformation;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.impl.MasterDataImportObjectCreationCountImpl;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.meta.LeaderboardGroupMetaLeaderboard;
import com.sap.sailing.domain.masterdataimport.TopLevelMasterData;
import com.sap.sailing.domain.masterdataimport.WindTrackMasterData;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.masterdata.DummyTrackedRace;

public class ImportMasterDataOperation extends
        AbstractRacingEventServiceOperation<MasterDataImportObjectCreationCountImpl> {

    private static final long serialVersionUID = 3131715325307370303L;

    private static final Logger logger = Logger.getLogger(ImportMasterDataOperation.class.getName());

    private final TopLevelMasterData masterData;

    private final MasterDataImportObjectCreationCountImpl creationCount;

    private final DomainFactory baseDomainFactory;

    private final boolean override;

    public ImportMasterDataOperation(TopLevelMasterData topLevelMasterData, boolean override,
            MasterDataImportObjectCreationCountImpl existingCreationCount, DomainFactory baseDomainFactory) {
        this.creationCount = new MasterDataImportObjectCreationCountImpl();
        this.creationCount.add(existingCreationCount);
        this.baseDomainFactory = baseDomainFactory;
        this.masterData = topLevelMasterData;
        this.override = override;
    }

    @Override
    public MasterDataImportObjectCreationCountImpl internalApplyTo(RacingEventService toState) throws Exception {
        toState.getDataImportLock().lock();
        try {
            for (LeaderboardGroup leaderboardGroup : masterData.getLeaderboardGroups()) {
                createLeaderboardGroupWithAllRelatedObjects(toState, leaderboardGroup);
            }
            createWindTracks(toState);
            return creationCount;
        } finally {
            toState.getDataImportLock().unlock();
        }
    }

    private void createLeaderboardGroupWithAllRelatedObjects(final RacingEventService toState,
            LeaderboardGroup leaderboardGroup) {
        Map<RaceColumn, Map<Fleet, Iterable<RaceLogEvent>>> raceLogEvents = extractRaceLogEvents(leaderboardGroup);
        Map<String, Leaderboard> existingLeaderboards = toState.getLeaderboards();
        List<String> leaderboardNames = new ArrayList<String>();
        createCourseAreasAndEvents(toState, leaderboardGroup);
        createRegattas(toState, leaderboardGroup);
        for (final Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
            leaderboardNames.add(leaderboard.getName());
            if (existingLeaderboards.containsKey(leaderboard.getName())) {
                if (creationCount.alreadyAddedLeaderboardWithName(leaderboard.getName())) {
                    //Has already been added by this operation
                    continue;
                } else if (override) {
                    for (RaceColumn raceColumn : existingLeaderboards.get(leaderboard.getName()).getRaceColumns()) {
                        for (Fleet fleet : raceColumn.getFleets()) {
                            TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                            if (trackedRace != null) {
                                raceColumn.releaseTrackedRace(fleet);
                            }
                        }
                    }
                    if (toState.getLeaderboardByName(leaderboard.getName()) != null) {
                        toState.removeLeaderboard(leaderboard.getName());
                    }
                    logger.info(String.format("Leaderboard with name %1$s already existed and has been overridden.",
                            leaderboard.getName()));
                } else {
                    logger.info(String.format("Leaderboard with name %1$s already exists and hasn't been overridden.",
                            leaderboard.getName()));
                    continue;
                }
            }
            if (leaderboard != null) {
                toState.addLeaderboard(leaderboard);
                addRaceLogEvents(leaderboard, raceLogEvents);
                creationCount.addOneLeaderboard(leaderboard.getName());
                relinkTrackedRacesIfPossible(toState, leaderboard);
            }

        }
        LeaderboardGroup existingLeaderboardGroup = toState.getLeaderboardGroupByName(leaderboardGroup.getName());
        if (existingLeaderboardGroup != null && override) {
            logger.info(String.format("Leaderboard Group with name %1$s already existed and will be overridden.",
                    leaderboardGroup.getName()));
            toState.removeLeaderboardGroup(leaderboardGroup.getName());
            existingLeaderboardGroup = null;
        }
        Leaderboard overallLeaderboardData = null;
        if (existingLeaderboardGroup == null) {
            overallLeaderboardData = leaderboardGroup.getOverallLeaderboard();
            int[] overallLeaderboardDiscardThresholds = null;
            ScoringSchemeType overallLeaderboardScoringSchemeType = null;
            if (overallLeaderboardData != null) {
                LeaderboardGroupMetaLeaderboard metaLeaderboard = (LeaderboardGroupMetaLeaderboard) overallLeaderboardData;
                ThresholdBasedResultDiscardingRule rule = (ThresholdBasedResultDiscardingRule) metaLeaderboard
                        .getResultDiscardingRule();
                overallLeaderboardDiscardThresholds = rule.getDiscardIndexResultsStartingWithHowManyRaces();
                overallLeaderboardScoringSchemeType = metaLeaderboard.getScoringScheme().getType();
            }
            leaderboardGroup = toState.addLeaderboardGroup(leaderboardGroup.getName(),
                    leaderboardGroup.getDescription(), leaderboardGroup.isDisplayGroupsInReverseOrder(),
                    leaderboardNames, overallLeaderboardDiscardThresholds,
                    overallLeaderboardScoringSchemeType);
            creationCount.addOneLeaderboardGroup(leaderboardGroup.getName());
        } else {
            leaderboardGroup = existingLeaderboardGroup;
            logger.info(String.format("Leaderboard Group with name %1$s already exists and hasn't been overridden.",
                    leaderboardGroup.getName()));
        }
        if (leaderboardGroup.getOverallLeaderboard() != null && (override || existingLeaderboardGroup == null)) {
            if (existingLeaderboardGroup != null && existingLeaderboardGroup.getOverallLeaderboard() != null) {
                // remove old overall leaderboard if it existed
                toState.removeLeaderboard(existingLeaderboardGroup.getOverallLeaderboard().getName());
            }
            Leaderboard overallLeaderboard = leaderboardGroup.getOverallLeaderboard();
            for (Competitor suppressedCompetitor : overallLeaderboardData.getSuppressedCompetitors()) {
                overallLeaderboard.setSuppressed(suppressedCompetitor, true);
            }
            for (RaceColumn column : overallLeaderboard.getRaceColumns()) {
                Double explicitFactor = overallLeaderboardData.getRaceColumnByName(column.getName())
                        .getExplicitFactor();
                toState.updateLeaderboardColumnFactor(overallLeaderboard.getName(), column.getName(), explicitFactor);
            }
            toState.getMongoObjectFactory().storeLeaderboardGroup(leaderboardGroup); // store changes to overall leaderboard
        }
    }

    private void addRaceLogEvents(Leaderboard leaderboard,
            Map<RaceColumn, Map<Fleet, Iterable<RaceLogEvent>>> raceLogEvents) {
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                RaceLog log = raceColumn.getRaceLog(fleet);
                Iterable<RaceLogEvent> raceLogsForColumnAndFleet = raceLogEvents.get(raceColumn).get(fleet);
                for (RaceLogEvent event : raceLogsForColumnAndFleet) {
                    log.add(event);
                }
            }
        }

    }

    private Map<RaceColumn, Map<Fleet, Iterable<RaceLogEvent>>> extractRaceLogEvents(LeaderboardGroup group) {
        Map<RaceColumn, Map<Fleet, Iterable<RaceLogEvent>>> raceLogEvents = new HashMap<RaceColumn, Map<Fleet, Iterable<RaceLogEvent>>>();
        for (Leaderboard leaderboard : group.getLeaderboards()) {
            for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                HashMap<Fleet, Iterable<RaceLogEvent>> raceLogEventsForRaceColumn = new HashMap<Fleet, Iterable<RaceLogEvent>>();
                raceLogEvents.put(raceColumn, raceLogEventsForRaceColumn);
                for (Fleet fleet : raceColumn.getFleets()) {
                    RaceLog raceLog = raceColumn.getRaceLog(fleet);
                    raceLog.lockForRead();
                    try {
                        Iterable<RaceLogEvent> fixes = raceLog.getRawFixes();
                        raceLogEventsForRaceColumn.put(fleet, fixes);
                    } finally {
                        raceLog.unlockAfterRead();
                    }
                }
            }
        }
        return raceLogEvents;
    }

    private void relinkTrackedRacesIfPossible(RacingEventService toState, Leaderboard newLeaderboard) {
        if (newLeaderboard instanceof FlexibleLeaderboard) {
            for (RaceColumn raceColumn : newLeaderboard.getRaceColumns()) {
                for (Fleet fleet : raceColumn.getFleets()) {
                    RaceIdentifier raceIdentifier = raceColumn.getRaceIdentifier(fleet);
                    if (raceIdentifier != null) {
                        DynamicTrackedRace trackedRace = toState
                                .getTrackedRace((RegattaAndRaceIdentifier) raceIdentifier);
                        raceColumn.setTrackedRace(fleet, trackedRace);
                    }
                }
            }
        }
    }

    /**
     * Hack adding a dummy tracked race, so that the competitors will be added to the leaderboards
     * 
     * @param leaderboard
     * @return the race column and fleet the dummy was attached to
     */
    public Pair<RaceColumn, Fleet> addDummyTrackedRace(Leaderboard leaderboard,
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
                DummyTrackedRace dummy = new DummyTrackedRace(leaderboard.getAllCompetitors(), regatta, null);
                raceColumn.setTrackedRace(fleet, dummy);
            }
        }
        return new Pair<RaceColumn, Fleet>(raceColumn, fleet);
    }

    private void createWindTracks(RacingEventService toState) {
        for (WindTrackMasterData windMasterData : masterData.getWindTrackMasterData()) {
            DummyTrackedRace trackedRaceWithNameAndId = new DummyTrackedRace(windMasterData.getRaceName(), windMasterData.getRaceId());
            WindTrack windTrack = toState.getWindStore().getWindTrack(windMasterData.getRegattaName(), trackedRaceWithNameAndId, windMasterData.getWindSource(), 0, -1);
            for (Wind fix : windMasterData.getFixes()) {
                windTrack.add(fix);
            }
        }
    }

    private void createRegattas(RacingEventService toState, LeaderboardGroup leaderboardGroup) {
        Iterable<Leaderboard> leaderboards = leaderboardGroup.getLeaderboards();
        for (Leaderboard leaderboard : leaderboards) {
            if (leaderboard instanceof RegattaLeaderboard) {
                RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
                Regatta regatta = regattaLeaderboard.getRegatta();

                Regatta existingRegatta = toState.getRegatta(regatta.getRegattaIdentifier());
                if (existingRegatta != null) {
                    if (creationCount.alreadyAddedRegattaWithId(existingRegatta.getId().toString())) {
                        // Already added earlier in this import process
                        continue;
                    } else if (override) {
                        logger.info(String
                                .format("Regatta with name %1$s already existed and has been overridden. All it's tracked races were stopped and removed.",
                                        regatta.getRegattaIdentifier()));
                        try {
                            TrackedRegatta trackedRegatta = toState.getTrackedRegatta(existingRegatta);
                            List<TrackedRace> toRemove = new ArrayList<TrackedRace>();
                            if (trackedRegatta != null) {
                                for (TrackedRace race : trackedRegatta.getTrackedRaces()) {
                                    toRemove.add(race);
                                }
                                for (TrackedRace raceToRemove : toRemove) {
                                    trackedRegatta.removeTrackedRace(raceToRemove);
                                    RaceDefinition race = existingRegatta.getRaceByName(raceToRemove
                                            .getRaceIdentifier().getRaceName());
                                    if (race != null) {
                                        try {
                                            toState.removeRace(existingRegatta, race);
                                        } catch (IOException | InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                            }
                            toState.stopTrackingAndRemove(existingRegatta);
                            creationCount.addOverwrittenRegattaName(existingRegatta.getName());
                            toState.removeRegatta(existingRegatta);
                        } catch (IOException | InterruptedException e) {
                            logger.warning(String.format(
                                    "Regatta with name %1$s could not be deleted due to an error.",
                                    regatta.getRegattaIdentifier()));
                            e.printStackTrace();
                            continue;
                        }
                    } else {
                        logger.info(String.format("Regatta with name %1$s already exists and hasn't been overridden.",
                                regatta.getRegattaIdentifier()));
                        continue;
                    }
                }
                Serializable id = regatta.getId();
                Iterable<? extends Series> series = regatta.getSeries();
                String baseName = regatta.getBaseName();
                String boatClassName = regatta.getBoatClass().getName();
                CourseArea defaultCourseArea = regatta.getDefaultCourseArea();
                Serializable defaultCourseAreaId = (defaultCourseArea != null) ? defaultCourseArea.getId() : null;
                ScoringSchemeType scoringSchemeType = regatta.getScoringScheme().getType();
                boolean isPersistent = regatta.isPersistent();
                Regatta createdRegatta = toState.getOrCreateRegattaWithoutReplication(baseName, boatClassName, id,
                        series, isPersistent, baseDomainFactory.createScoringScheme(scoringSchemeType),
                        defaultCourseAreaId).getA();
                createdRegatta.setRegattaConfiguration(regatta.getRegattaConfiguration());
                Set<String> raceIdStrings = masterData.getRaceIdStringsForRegatta().get(regatta.getRegattaIdentifier());
                if (raceIdStrings != null) {
                    toState.setPersistentRegattaForRaceIDs(createdRegatta, raceIdStrings, override);
                }
                creationCount.addOneRegatta(createdRegatta.getId().toString());
            }
        }

    }


    private void createCourseAreasAndEvents(RacingEventService toState, LeaderboardGroup leaderboardGroup) {
        for (Event event : masterData.getEventForLeaderboardGroup().get(leaderboardGroup)) {
            UUID id = event.getId();
            Event existingEvent = toState.getEvent(id);
            if (existingEvent != null && override && !creationCount.alreadyAddedEventWithId(id.toString())) {
                logger.info(String.format("Event with name %1$s already existed and will be overridden.",
                        event.getName()));
                toState.removeEvent(existingEvent.getId());
                existingEvent = null;
            }
            if (existingEvent == null) {
                String name = event.getName();
                String pubString = event.getPublicationUrl();
                String venueName = event.getVenue().getName();
                boolean isPublic = event.isPublic();
                Event newEvent = toState.createEventWithoutReplication(name, venueName, pubString, isPublic, id);
                creationCount.addOneEvent(newEvent.getId().toString());
            } else {
                logger.info(String.format("Event with name %1$s already exists and hasn't been overridden.",
                        event.getName()));
            }
            Iterable<CourseArea> courseAreas = event.getVenue().getCourseAreas();
            for (CourseArea courseArea : courseAreas) {
                boolean alreadyExists = false;
                if (existingEvent != null && existsInSet(existingEvent.getVenue().getCourseAreas(), courseArea.getId())) {
                    alreadyExists = true;
                }
                if (!alreadyExists) {
                    toState.addCourseAreaWithoutReplication(id, courseArea.getId(), courseArea.getName());
                } else {
                    logger.info(String
                            .format("Course area with id %1$s for event with id %2$s already exists and hasn't been overridden.",
                                    courseArea.getId(), id));
                }

            }
        }
    }

    /**
     * 
     * @param iterable
     * @param uuid
     * @return true if course with given id exists in iterable
     */
    private boolean existsInSet(Iterable<CourseArea> iterable, UUID uuid) {
        for (CourseArea area : iterable) {
            if (area.getId() == uuid) {
                return true;
            }
        }
        return false;
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
