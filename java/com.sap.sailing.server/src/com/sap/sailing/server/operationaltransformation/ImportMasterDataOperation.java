package com.sap.sailing.server.operationaltransformation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.impl.MasterDataImportObjectCreationCountImpl;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.masterdataimport.TopLevelMasterData;
import com.sap.sailing.domain.masterdataimport.WindTrackMasterData;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.masterdata.DummyTrackedRace;
import com.sap.sse.common.Util;

public class ImportMasterDataOperation extends
        AbstractRacingEventServiceOperation<MasterDataImportObjectCreationCountImpl> {

    private static final long serialVersionUID = 3131715325307370303L;

    private static final Logger logger = Logger.getLogger(ImportMasterDataOperation.class.getName());

    private final TopLevelMasterData masterData;

    private final MasterDataImportObjectCreationCountImpl creationCount;

    private final boolean override;

    private final UUID importOperationId;

    private DataImportProgress progress;

    public ImportMasterDataOperation(TopLevelMasterData topLevelMasterData, UUID importOperationId, boolean override,
            MasterDataImportObjectCreationCountImpl existingCreationCount) {
        this.creationCount = new MasterDataImportObjectCreationCountImpl();
        this.creationCount.add(existingCreationCount);
        this.masterData = topLevelMasterData;
        this.override = override;
        this.importOperationId = importOperationId;
    }

    @Override
    public MasterDataImportObjectCreationCountImpl internalApplyTo(RacingEventService toState) throws Exception {
        this.progress = toState.getDataImportLock().getProgress(importOperationId);
        progress.setNameOfCurrentSubProgress("Waiting for other data import operations to finish");
        toState.getDataImportLock().lock();
        try {
            progress.setNameOfCurrentSubProgress("Importing leaderboard groups");
            progress.setCurrentSubProgressPct(0);
            int numOfGroupsToImport = masterData.getLeaderboardGroups().size();
            int i = 0;
            for (LeaderboardGroup leaderboardGroup : masterData.getLeaderboardGroups()) {
                createLeaderboardGroupWithAllRelatedObjects(toState, leaderboardGroup);
                i++;
                progress.setCurrentSubProgressPct((double) i / numOfGroupsToImport);
            }
            progress.setNameOfCurrentSubProgress("Updating Event-LeaderboardGroup links");
            progress.setOverAllProgressPct(0.4);
            progress.setCurrentSubProgressPct(0);
            final Iterable<Event> allEvents = masterData.getAllEvents();
            int numOfEventsToHandle = Util.size(allEvents);
            int eventCounter = 0;
            for (Event e : allEvents) {
                updateLinksToLeaderboardGroups(toState, e);
                eventCounter++;
                progress.setCurrentSubProgressPct((double) eventCounter / numOfEventsToHandle);
            }
            progress.setNameOfCurrentSubProgress("Importing wind tracks");
            progress.setOverAllProgressPct(0.5);
            progress.setCurrentSubProgressPct(0);
            createWindTracks(toState);
            toState.getDataImportLock().getProgress(importOperationId).setResult(creationCount);
            return creationCount;
        } catch (Exception e) {
            logger.severe("Error during execution of ImportMasterDataOperation");
            throw new RuntimeException("Error during execution of ImportMasterDataOperation", e);
        } finally {
            toState.getDataImportLock().unlock();
        }
    }

    /**
     * Ensures that all links from <code>eventReceived</code> to its leaderboard groups are established also on the
     * local event after import as long as those leaderboard groups are part of the actual import. For this subset of
     * leaderboard groups, equality of ordering is established between the <code>eventReceived</code>'s leaderboard
     * group sequence and the local event's leaderboard group sequence. This may require temporarily removing
     * leaderboard groups from the local event and re-adding them at the end which may change the ordering with respect
     * to other, non-imported leaderboard groups.
     * <p>
     * 
     * Loops over the imported event's leaderboard groups and for those part of the import tries to find by ID each of
     * them in the local event's leaderboard group sequence. If not found, it is appended at the end. If found after the
     * position of the previous leaderboard group handled, it is left in place. Otherwise, it is removed and added again
     * at the end.
     */
    private void updateLinksToLeaderboardGroups(RacingEventService racingEventService, Event eventReceived) {
        boolean changed = false;
        int positionOfLastLeaderboardGroupFoundInLocalEvent = -1;
        Event eventAfterImport = racingEventService.getEvent(eventReceived.getId());
        Collection<LeaderboardGroup> leaderboardGroupsReceived = masterData.getLeaderboardGroups();
        for (LeaderboardGroup lgInEventReceived : eventReceived.getLeaderboardGroups()) {
            if (leaderboardGroupsReceived.contains(lgInEventReceived)) {
                // it shall also be referenced by eventAfterImport, with a position that shall be greater than
                // positionOfLastLeaderboardGroupFoundInLocalEvent.
                int pos = 0;
                boolean found = false;
                for (LeaderboardGroup importedLg : eventAfterImport.getLeaderboardGroups()) {
                    if (importedLg.getId().equals(lgInEventReceived.getId())) {
                        found = true;
                        if (pos < positionOfLastLeaderboardGroupFoundInLocalEvent) {
                            // need to move lgInEventReceived; move to end
                            eventAfterImport.removeLeaderboardGroup(importedLg);
                            eventAfterImport.addLeaderboardGroup(importedLg);
                            positionOfLastLeaderboardGroupFoundInLocalEvent = Util.size(eventAfterImport.getLeaderboardGroups())-1;
                            changed = true;
                        } else {
                            positionOfLastLeaderboardGroupFoundInLocalEvent = pos;
                        }
                        break;
                    }
                    pos++;
                }
                if (!found) {
                    eventAfterImport.addLeaderboardGroup(racingEventService.getLeaderboardGroupByID(lgInEventReceived.getId()));
                    positionOfLastLeaderboardGroupFoundInLocalEvent = Util.size(eventAfterImport.getLeaderboardGroups())-1;
                    changed = true;
                }
            }
        }
        if (changed) {
            racingEventService.getMongoObjectFactory().storeEvent(eventAfterImport);
        }
    }

    private void createLeaderboardGroupWithAllRelatedObjects(final RacingEventService toState,
            LeaderboardGroup leaderboardGroup) {
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
                storeRaceLogEvents(leaderboard, toState.getMongoObjectFactory());
                creationCount.addOneLeaderboard(leaderboard.getName());
                relinkTrackedRacesIfPossible(toState, leaderboard);
                toState.updateStoredLeaderboard(leaderboard);
            }

        }
        // TODO bug 1975: as an aftermath of bug 1970, with LeaderboardGroup now implementing WithID, match making could happen by ID
        LeaderboardGroup existingLeaderboardGroup = toState.getLeaderboardGroupByName(leaderboardGroup.getName());
        if (existingLeaderboardGroup != null && override) {
            logger.info(String.format("Leaderboard Group with name %1$s already existed and will be overridden.",
                    leaderboardGroup.getName()));
            toState.removeLeaderboardGroup(leaderboardGroup.getName());
            existingLeaderboardGroup = null;
        }
        if (existingLeaderboardGroup == null) {
            toState.addLeaderboardGroupWithoutReplication(leaderboardGroup);
            creationCount.addOneLeaderboardGroup(leaderboardGroup.getName());
        } else {
            logger.info(String.format("Leaderboard Group with name %1$s already exists and hasn't been overridden.",
                    leaderboardGroup.getName()));
        }
    }

    /**
     * Ensures that the race log events are stored to the receiving instance's database. The race logs have been received
     * in serialized form on the {@link RaceColumn} objects, but the database doesn't yet know about them. This method uses
     * a <code>MongoRaceLogStoreVisitor</code> to store all race log events to the database.
     */
    private void storeRaceLogEvents(Leaderboard leaderboard, MongoObjectFactory mongoObjectFactory) {
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                RaceLog log = raceColumn.getRaceLog(fleet);
                if (log != null) {
                    RaceLogIdentifier identifier = raceColumn.getRaceLogIdentifier(fleet);
                    RaceLogEventVisitor storeVisitor = MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStoreVisitor(
                            identifier, mongoObjectFactory);
                    log.lockForRead();
                    try {
                        for (RaceLogEvent event : log.getRawFixes()) {
                            event.accept(storeVisitor);
                        }
                    } finally {
                        log.unlockAfterRead();
                    }
                }
            }
        }

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
    public com.sap.sse.common.Util.Pair<RaceColumn, Fleet> addDummyTrackedRace(Leaderboard leaderboard,
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
                DummyTrackedRace dummy = new DummyTrackedRace(UUID.randomUUID(), leaderboard.getAllCompetitors(), regatta, null, EmptyWindStore.INSTANCE);
                raceColumn.setTrackedRace(fleet, dummy);
            }
        }
        return new com.sap.sse.common.Util.Pair<RaceColumn, Fleet>(raceColumn, fleet);
    }

    private void createWindTracks(RacingEventService toState) {
        int numOfWindTracks = masterData.getWindTrackMasterData().size();
        int i = 0;
        for (WindTrackMasterData windMasterData : masterData.getWindTrackMasterData()) {
            DummyTrackedRace trackedRaceWithNameAndId = new DummyTrackedRace(windMasterData.getRaceName(), windMasterData.getRaceId());
            WindTrack windTrackToWriteTo = toState.getWindStore().getWindTrack(windMasterData.getRegattaName(), trackedRaceWithNameAndId, windMasterData.getWindSource(), 0, -1);
            final WindTrack windTrackToReadFrom = windMasterData.getWindTrack();
            windTrackToReadFrom.lockForRead();
            try {
                for (Wind fix : windTrackToReadFrom.getRawFixes()) {
                    windTrackToWriteTo.add(fix);
                }
            } finally {
                windTrackToReadFrom.unlockAfterRead();
            }
            i++;
            progress.setCurrentSubProgressPct((double) i / numOfWindTracks);
            progress.setOverAllProgressPct(0.5 + (0.5) * ((double) i / numOfWindTracks));
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
                toState.addRegattaWithoutReplication(regatta);
                Set<String> raceIdStrings = masterData.getRaceIdStringsForRegatta().get(regatta.getRegattaIdentifier());
                if (raceIdStrings != null) {
                    for (String raceIdAsString : raceIdStrings) {
                        if (!override && toState.getRememberedRegattaForRace(raceIdAsString) != null) {
                            logger.info(String
                                    .format("Persistent regatta wasn't set for race id %1$s, because override was not turned on.",
                                            raceIdAsString));
                        } else {
                            toState.setRegattaForRace(regatta, raceIdAsString);
                        }
                    }
                }
                creationCount.addOneRegatta(regatta.getId().toString());
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
                toState.addEventWithoutReplication(event);
                creationCount.addOneEvent(event.getId().toString());
            } else {
                logger.info(String.format("Event with name %1$s already exists and hasn't been overridden.",
                        event.getName()));
            }
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

    /**
     * in order to restore all listeners we need to initialize the regatta after the whole object graph has been
     * restored. This applies to all replicas that receive this operation "over the wire".
     * 
     * Fixes bug2023
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        for (Regatta regatta : masterData.getAllRegattas()) {
            RegattaImpl regattaImpl = (RegattaImpl) regatta;
            regattaImpl.initializeSeriesAfterDeserialize();
        }
    }

}
