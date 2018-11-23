package com.sap.sailing.server.operationaltransformation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.domain.common.DataImportSubProgress;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.MasterDataImportObjectCreationCountImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.common.tracking.impl.VeryCompactGPSFixImpl;
import com.sap.sailing.domain.common.tracking.impl.VeryCompactGPSFixMovingImpl;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.masterdataimport.TopLevelMasterData;
import com.sap.sailing.domain.masterdataimport.WindTrackMasterData;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.persistence.MongoRegattaLogStoreFactory;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.regattalike.HasRegattaLike;
import com.sap.sailing.domain.regattalike.IsRegattaLike;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.masterdata.DataImportLockWithProgress;
import com.sap.sailing.server.masterdata.DummyTrackedRace;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.Timed;
import com.sap.sse.common.Util;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;

public class ImportMasterDataOperation extends
        AbstractRacingEventServiceOperation<MasterDataImportObjectCreationCountImpl> {

    private static final long serialVersionUID = 3131715325307370303L;

    private static final Logger logger = Logger.getLogger(ImportMasterDataOperation.class.getName());
    
    private static final int BATCH_SIZE_FOR_IMPORTING_FIXES = 5000;

    private final TopLevelMasterData masterData;

    private final MasterDataImportObjectCreationCountImpl creationCount;

    private final boolean override;

    private final UUID importOperationId;

    private DataImportProgress progress;

    private User user;

    private UserGroup tenant;

    public ImportMasterDataOperation(TopLevelMasterData topLevelMasterData, UUID importOperationId, boolean override,
            MasterDataImportObjectCreationCountImpl existingCreationCount, User user, UserGroup tenant) {
        this.creationCount = new MasterDataImportObjectCreationCountImpl();
        this.creationCount.add(existingCreationCount);
        this.masterData = topLevelMasterData;
        this.override = override;
        this.importOperationId = importOperationId;
        this.user = user;
        this.tenant = tenant;
    }

    @Override
    public MasterDataImportObjectCreationCountImpl internalApplyTo(RacingEventService toState) throws Exception {
        final DataImportLockWithProgress dataImportLock = toState.getDataImportLock();
        SecurityService securityService = toState.getSecurityService();
        if (securityService == null) {
            throw new IllegalStateException("Cannot import data, security service could not be resolved");
        }
        this.progress = dataImportLock.getProgress(importOperationId);
        progress.setCurrentSubProgress(DataImportSubProgress.IMPORT_WAIT);
        LockUtil.lockForWrite(dataImportLock);
        try {
            progress.setCurrentSubProgress(DataImportSubProgress.IMPORT_LEADERBOARD_GROUPS);
            progress.setCurrentSubProgressPct(0);
            int numOfGroupsToImport = masterData.getLeaderboardGroups().size();
            int i = 0;
            for (LeaderboardGroup leaderboardGroup : masterData.getLeaderboardGroups()) {
                createLeaderboardGroupWithAllRelatedObjects(toState, leaderboardGroup, securityService);
                i++;
                progress.setCurrentSubProgressPct((double) i / numOfGroupsToImport);
            }
            progress.setCurrentSubProgress(DataImportSubProgress.UPDATE_EVENT_LEADERBOARD_GROUP_LINKS);
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
            progress.setCurrentSubProgress(DataImportSubProgress.IMPORT_WIND_TRACKS);
            progress.setOverAllProgressPct(0.5);
            progress.setCurrentSubProgressPct(0);
            createWindTracks(toState);
            progress.setCurrentSubProgress(DataImportSubProgress.IMPORT_SENSOR_FIXES);
            progress.setOverAllProgressPct(0.8);
            progress.setCurrentSubProgressPct(0);
            importRaceLogTrackingGPSFixes(toState);
            if (masterData.getDeviceConfigurations() != null) {
                importDeviceConfigurations(toState);
            }
            toState.mediaTracksImported(masterData.getFilteredMediaTracks(), creationCount, override);
            dataImportLock.getProgress(importOperationId).setResult(creationCount);
            return creationCount;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during execution of ImportMasterDataOperation", e);
            throw new RuntimeException("Error during execution of ImportMasterDataOperation", e);
        } finally {
            LockUtil.unlockAfterWrite(dataImportLock);
        }
    }

    private void importDeviceConfigurations(RacingEventService toState) {
        Map<DeviceConfigurationMatcher, DeviceConfiguration> existingConfigs = toState.getAllDeviceConfigurations();
        Set<DeviceConfigurationMatcher> existingKeys = existingConfigs.keySet();
        Map<DeviceConfigurationMatcher, DeviceConfiguration> newConfigs = masterData.getDeviceConfigurations();
        for(Entry<DeviceConfigurationMatcher, DeviceConfiguration> entry : newConfigs.entrySet()) {
            DeviceConfigurationMatcher key = entry.getKey();
            DeviceConfiguration value = entry.getValue();
            if (existingKeys.contains(key)) {
                if (override) {
                    logger.info(String.format(
                            "Device configuration [%s] already exists. Overwrite because override flag is set.",
                            key.getMatcherIdentifier()));
                    toState.removeDeviceConfiguration(key);
                    toState.createOrUpdateDeviceConfiguration(key, value);
                    // FIXME ownership here!
                } else {
                    logger.info(String
                            .format("Device configuration [%s] already exists. Not overwriting because override flag is not set.",
                                    key.getMatcherIdentifier()));
                }
            } else {
                toState.createOrUpdateDeviceConfiguration(key, value);
            }
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
            LeaderboardGroup leaderboardGroup, SecurityService securityService) {
        Map<String, Leaderboard> existingLeaderboards = toState.getLeaderboards();
        List<String> leaderboardNames = new ArrayList<String>();
        createCourseAreasAndEvents(toState, leaderboardGroup, securityService);
        createRegattas(toState, leaderboardGroup, securityService);
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
                storeRaceLogEvents(leaderboard, toState.getMongoObjectFactory(), toState.getDomainObjectFactory(), override);
                storeRegattaLogEvents(leaderboard, toState.getMongoObjectFactory(), toState.getDomainObjectFactory(), override);
                ensureOwnership(leaderboard.getIdentifier(), securityService);
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
            ensureOwnership(leaderboardGroup.getIdentifier(), securityService);
        } else {
            logger.info(String.format("Leaderboard Group with name %1$s already exists and hasn't been overridden.",
                    leaderboardGroup.getName()));
        }
    }

    /**
     * Ensures that the race log events are stored to the receiving instance's database. The race logs have been
     * received in serialized form on the {@link RaceColumn} objects, but the database doesn't yet know about them. This
     * method uses a <code>MongoRaceLogStoreVisitor</code> to store all race log events to the database.
     */
    private void storeRaceLogEvents(Leaderboard leaderboard, MongoObjectFactory mongoObjectFactory,
            DomainObjectFactory domainObjectFactory, boolean override) {
        RaceLogStore mongoRaceLogStore = MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mongoObjectFactory,
                domainObjectFactory);
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                RaceLog log = raceColumn.getRaceLog(fleet);
                if (log != null) {
                    RaceLogIdentifier identifier = raceColumn.getRaceLogIdentifier(fleet);
                    RaceLog currentPersistedLog = mongoRaceLogStore.getRaceLog(identifier, true);
                    if (currentPersistedLog.isEmpty()) {
                        addAllImportedEvents(mongoObjectFactory, mongoRaceLogStore, log, identifier);
                    } else if (override) {
                        // Clear existing race log
                        mongoRaceLogStore.removeRaceLog(identifier);
                        addAllImportedEvents(mongoObjectFactory, mongoRaceLogStore, log, identifier);
                    }
                }
            }
        }
    }

    private void addAllImportedEvents(MongoObjectFactory mongoObjectFactory, RaceLogStore mongoRaceLogStore,
            RaceLog log, RaceLogIdentifier identifier) {
        RaceLogEventVisitor storeVisitor = MongoRaceLogStoreFactory.INSTANCE
                .getMongoRaceLogStoreVisitor(identifier, mongoObjectFactory);
        log.lockForRead();
        try {
            for (RaceLogEvent event : log.getRawFixes()) {
                event.accept(storeVisitor);
            }
        } finally {
            log.unlockAfterRead();
        }
        // Make sure listener is added to race log
        mongoRaceLogStore.addImportedRaceLog(log, identifier);
    }

    /**
     * Ensures that the regatta log events are stored to the receiving instance's database. The {@code leaderboard}
     * potentially {@link HasRegattaLike has} an attached RegattaLog, which then must be stored in the database.
     * @see #storeRaceLogEvents(Leaderboard, MongoObjectFactory)
     */
    private void storeRegattaLogEvents(Leaderboard leaderboard, MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory,
            boolean override) {
        RegattaLogStore regattaLogStore = MongoRegattaLogStoreFactory.INSTANCE.getMongoRegattaLogStore(mongoObjectFactory, domainObjectFactory);
        if (leaderboard instanceof HasRegattaLike) {
            IsRegattaLike regattaLike = ((HasRegattaLike) leaderboard).getRegattaLike();
            RegattaLog log = regattaLike.getRegattaLog();
            RegattaLikeIdentifier identifier = regattaLike.getRegattaLikeIdentifier();
            RegattaLogEventVisitor storeVisitor = MongoRegattaLogStoreFactory.INSTANCE.getMongoRegattaLogStoreVisitor(
                    identifier, mongoObjectFactory);
            RegattaLog currentPersistedLog = regattaLogStore.getRegattaLog(identifier, true);
            if (currentPersistedLog.isEmpty()) {
                addAllImportedRegattaEvents(regattaLogStore, log, identifier, storeVisitor);
            } else if (override) {
                //Clear existing regatta log
                regattaLogStore.removeRegattaLog(identifier);
                addAllImportedRegattaEvents(regattaLogStore, log, identifier, storeVisitor);
            }
        }
    }

    private void addAllImportedRegattaEvents(RegattaLogStore regattaLogStore, RegattaLog log,
            RegattaLikeIdentifier identifier, RegattaLogEventVisitor storeVisitor) {
        log.lockForRead();
        try {
            for (RegattaLogEvent event : log.getRawFixes()) {
                event.accept(storeVisitor);
            }
        } finally {
            log.unlockAfterRead();
        }
        regattaLogStore.addImportedRegattaLog(log, identifier);
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
                    Wind existingFix = windTrackToWriteTo.getFirstRawFixAtOrAfter(fix.getTimePoint());
                    if (existingFix == null || !(existingFix.equals(fix) && fix.getTimePoint().equals(existingFix.getTimePoint())
                            && Util.equalsWithNull(fix.getPosition(), existingFix.getPosition()))) {
                        windTrackToWriteTo.add(fix);
                    } else {
                        logger.info("Didn't add wind fix in import, because equal fix was already there.");
                    }
                }
            } finally {
                windTrackToReadFrom.unlockAfterRead();
            }
            i++;
            progress.setCurrentSubProgressPct((double) i / numOfWindTracks);
            progress.setOverAllProgressPct(0.5 + (0.3) * ((double) i / numOfWindTracks));
        }
    }
    

    
    private void importRaceLogTrackingGPSFixes(RacingEventService toState) {
        Map<DeviceIdentifier, Set<Timed>> raceLogTrackingFixes = masterData.getRaceLogTrackingFixes();
        if (raceLogTrackingFixes != null) {
            SensorFixStore store = toState.getSensorFixStore();
            int i = 0;
            final int numberOfDevices = raceLogTrackingFixes.size();
            for (Entry<DeviceIdentifier, Set<Timed>> entry : raceLogTrackingFixes.entrySet()) {
                DeviceIdentifier device = entry.getKey();
                final Collection<Timed> fixesToAddAsBatch = new ArrayList<>(BATCH_SIZE_FOR_IMPORTING_FIXES);
                for (Timed fixToAdd : entry.getValue()) {
                    if (fixToAdd instanceof VeryCompactGPSFixMovingImpl) {
                        VeryCompactGPSFixMovingImpl gpsFix = (VeryCompactGPSFixMovingImpl) fixToAdd;
                        fixToAdd = new GPSFixMovingImpl(gpsFix.getPosition(), fixToAdd.getTimePoint(),
                                ((VeryCompactGPSFixMovingImpl) fixToAdd).getSpeed());
                    } else if (fixToAdd instanceof VeryCompactGPSFixImpl) {
                        VeryCompactGPSFixImpl gpsFix = (VeryCompactGPSFixImpl) fixToAdd;
                        fixToAdd = new GPSFixImpl(gpsFix.getPosition(), fixToAdd.getTimePoint());
                    } 
                    fixesToAddAsBatch.add(fixToAdd);
                    if (fixesToAddAsBatch.size() == BATCH_SIZE_FOR_IMPORTING_FIXES) {
                        storeFixes(store, device, fixesToAddAsBatch);
                    }
                }
                if (!fixesToAddAsBatch.isEmpty()) {
                    storeFixes(store, device, fixesToAddAsBatch);
                }
                i++;
                progress.setCurrentSubProgressPct((double) i / numberOfDevices);
            }
        }
    }

    private void storeFixes(SensorFixStore store, DeviceIdentifier device, final Collection<Timed> fixesToAddAsBatch) {
        try {
            store.storeFixes(device, fixesToAddAsBatch);
            fixesToAddAsBatch.clear();
        } catch (NoCorrespondingServiceRegisteredException e) {
            logger.severe("Failed to store race log tracking fixes while importing.");
            e.printStackTrace();
        }
    }

    private void createRegattas(RacingEventService toState, LeaderboardGroup leaderboardGroup,
            SecurityService securityService) {
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
                                trackedRegatta.lockTrackedRacesForRead();
                                try {
                                    for (TrackedRace race : trackedRegatta.getTrackedRaces()) {
                                        toRemove.add(race);
                                    }
                                } finally {
                                    trackedRegatta.unlockTrackedRacesAfterRead();
                                }
                                for (TrackedRace raceToRemove : toRemove) {
                                    trackedRegatta.removeTrackedRace(raceToRemove, Optional.of(toState
                                            .getThreadLocalTransporterForCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster()));
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
                ensureOwnership(regatta.getIdentifier(), securityService);
                creationCount.addOneRegatta(regatta.getId().toString());
            }
        }

    }


    private void createCourseAreasAndEvents(RacingEventService toState, LeaderboardGroup leaderboardGroup,
            SecurityService securityService) {
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
                ensureOwnership(event.getIdentifier(), securityService);
                creationCount.addOneEvent(event.getId().toString());
            } else {
                logger.info(String.format("Event with name %1$s already exists and hasn't been overridden.",
                        event.getName()));
            }
        }
    }

    private void ensureOwnership(QualifiedObjectIdentifier identifier, SecurityService securityService) {
        securityService.setOwnershipIfNotSet(identifier, user, tenant);
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
