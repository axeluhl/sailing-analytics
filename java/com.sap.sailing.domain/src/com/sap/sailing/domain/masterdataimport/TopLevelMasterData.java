package com.sap.sailing.domain.masterdataimport;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.Timed;
import com.sap.sse.common.Util;

/**
 * Holds all information needed for a master data import.
 * 
 * @author Frederik Petersen (D054528)
 * 
 */
public class TopLevelMasterData implements Serializable {
    
    private static final Logger logger = Logger.getLogger(TopLevelMasterData.class.getName());

    private static final long serialVersionUID = 4820893865792553281L;
    private final Map<RegattaIdentifier, Set<String>> raceIdStringsForRegatta;
    private final Set<MediaTrack> filteredMediaTracks;
    private final Set<LeaderboardGroup> leaderboardGroups;
    private final Set<WindTrackMasterData> windTrackMasterData;
    private final Map<LeaderboardGroup, Set<Event>> eventForLeaderboardGroup;
    private final Map<DeviceIdentifier, Set<Timed>> raceLogTrackingFixes;
    private final Map<DeviceConfigurationMatcher, DeviceConfiguration> deviceConfigurations;

    public TopLevelMasterData(final Set<LeaderboardGroup> groupsToExport, final Iterable<Event> allEvents,
            final Map<String, Regatta> regattaForRaceIdString, final Iterable<MediaTrack> allMediaTracks,
            SensorFixStore sensorFixStore, boolean exportWind,
            Map<DeviceConfigurationMatcher, DeviceConfiguration> deviceConfigurations) {
        this.raceIdStringsForRegatta = convertToRaceIdStringsForRegattaMap(regattaForRaceIdString);
        this.leaderboardGroups = groupsToExport;
        this.raceLogTrackingFixes = getAllRelevantRaceLogTrackingFixes(sensorFixStore);
        if (exportWind) {
            this.windTrackMasterData = fillWindMap(groupsToExport);
        } else {
            this.windTrackMasterData = new HashSet<WindTrackMasterData>();
        }
        this.deviceConfigurations = deviceConfigurations;
        this.eventForLeaderboardGroup = createEventMap(groupsToExport, allEvents);
        this.filteredMediaTracks = new HashSet<MediaTrack>();
        filterMediaTracks(allMediaTracks, this.filteredMediaTracks);
    }

    private Map<DeviceIdentifier, Set<Timed>> getAllRelevantRaceLogTrackingFixes(SensorFixStore sensorFixStore) {
        Map<DeviceIdentifier, Set<Timed>> relevantFixes = new HashMap<>();
        // Add fixes for regatta log mappings
        for (Regatta regatta : getAllRegattas()) {
            RegattaLog regattaLog = regatta.getRegattaLog();
            try {
                regattaLog.lockForRead();
                for (RegattaLogEvent logEvent : regattaLog.getRawFixes()) {
                    addAllFixesIfMappingEvent(sensorFixStore, relevantFixes, logEvent);
                }
            } finally {
                regattaLog.unlockAfterRead();
            }
        }
        // Add fixes for race log mapping
        for (LeaderboardGroup group : leaderboardGroups) {
            for (Leaderboard leaderboard : group.getLeaderboards()) {
                for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                    for (Fleet fleet : raceColumn.getFleets()) {
                        RaceLog raceLog = raceColumn.getRaceLog(fleet);
                        try {
                            raceLog.lockForRead();
                            for (RaceLogEvent logEvent : raceLog.getRawFixes()) {
                                addAllFixesIfMappingEvent(sensorFixStore, relevantFixes, logEvent);
                            }
                        } finally {
                            raceLog.unlockAfterRead();
                        }
                    }
                }
            }
        }
        return relevantFixes;
    }

    private void addAllFixesIfMappingEvent(SensorFixStore sensorFixStore,
            Map<DeviceIdentifier, Set<Timed>> relevantFixes,
            AbstractLogEvent<?> logEvent) {
        if (logEvent instanceof RegattaLogDeviceMappingEvent<?>) {
            RegattaLogDeviceMappingEvent<?> mappingEvent = (RegattaLogDeviceMappingEvent<?>) logEvent;
            try {
                addAllFixesForMappingEvent(sensorFixStore, relevantFixes, mappingEvent);
            } catch (NoCorrespondingServiceRegisteredException | TransformationException e) {
                logger.severe("Failed to add fixes to exportdata for mapping Event");
                e.printStackTrace();
            }
        }
    }

    private void addAllFixesForMappingEvent(SensorFixStore sensorFixStore,
            Map<DeviceIdentifier, Set<Timed>> relevantFixes,
            RegattaLogDeviceMappingEvent<?> mappingEvent) throws NoCorrespondingServiceRegisteredException, TransformationException {
        DeviceIdentifier device = mappingEvent.getDevice();
        if (!relevantFixes.containsKey(device)) {
            relevantFixes.put(device, new HashSet<>());
        }
        Set<Timed> fixes = relevantFixes.get(device);
        sensorFixStore.loadFixes(fixes::add, mappingEvent.getDevice(), mappingEvent.getFrom(), mappingEvent.getToInclusive(),
                true);
    }

    /**
     * Workaround to look for the events connected to RegattaLeadeboards. There should be a proper connection between
     * regatta and event soon. TODO
     */
    private Map<LeaderboardGroup, Set<Event>> createEventMap(Set<LeaderboardGroup> groupsToExport,
            Iterable<Event> allEvents) {
        Map<LeaderboardGroup, Set<Event>> eventsForLeaderboardGroup = new HashMap<LeaderboardGroup, Set<Event>>();
        Map<CourseArea, Event> eventForCourseArea = new HashMap<CourseArea, Event>();
        for (Event event : allEvents) {
            for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
                eventForCourseArea.put(courseArea, event);
            }
        }
        for (LeaderboardGroup leaderboardGroup : groupsToExport) {
            HashSet<Event> eventSet = new HashSet<Event>();
            eventsForLeaderboardGroup.put(leaderboardGroup, eventSet);
            for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                CourseArea courseArea = leaderboard.getDefaultCourseArea();
                if (courseArea != null) {
                    Event event = eventForCourseArea.get(courseArea);
                    if (event != null) {
                        eventSet.add(event);
                    }
                }
            }
        }
        return eventsForLeaderboardGroup;
    }

    private Map<RegattaIdentifier, Set<String>> convertToRaceIdStringsForRegattaMap(
            Map<String, Regatta> regattaForRaceIdString) {
        Map<RegattaIdentifier, Set<String>> raceIdStringsForRegatta = new HashMap<RegattaIdentifier, Set<String>>();
        for (Entry<String, Regatta> entry : regattaForRaceIdString.entrySet()) {
            Regatta regatta = entry.getValue();
            Set<String> raceIds = raceIdStringsForRegatta.get(regatta.getRegattaIdentifier());
            if (raceIds == null) {
                raceIds = new HashSet<String>();
                raceIdStringsForRegatta.put(regatta.getRegattaIdentifier(), raceIds);
            }
            raceIds.add(entry.getKey());
        }
        return raceIdStringsForRegatta;
    }

    private Set<WindTrackMasterData> fillWindMap(Set<LeaderboardGroup> groupsToExport) {
        Set<WindTrackMasterData> windTrackMasterDataSet = new HashSet<WindTrackMasterData>();
        for (LeaderboardGroup group : groupsToExport) {
            for (Leaderboard leaderboard : group.getLeaderboards()) {
                for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                    for (Fleet fleet : raceColumn.getFleets()) {
                        addWindTracksToSetIfExistantAndSourceCanBeStored(windTrackMasterDataSet, raceColumn, fleet);
                    }
                }
            }
        }
        return windTrackMasterDataSet;
    }

    private void addWindTracksToSetIfExistantAndSourceCanBeStored(Set<WindTrackMasterData> windTrackMasterDataSet,
            RaceColumn raceColumn, Fleet fleet) {
        TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
        if (trackedRace != null) {
            Iterable<WindSource> windSources = trackedRace.getWindSources();
            String raceName = trackedRace.getRace().getName();
            Serializable raceId = trackedRace.getRace().getId();
            String regattaName = trackedRace.getTrackedRegatta().getRegatta().getName();
            if (windSources != null) {
                for (WindSource source : windSources) {
                    if (source.canBeStored()) {
                        windTrackMasterDataSet.add(new WindTrackMasterData(source.getType(), source.getId(),
                                trackedRace.getOrCreateWindTrack(source), regattaName, raceName, raceId));
                    }
                }
            }
        }
    }

    public Collection<MediaTrack> getFilteredMediaTracks() {
        return this.filteredMediaTracks;
    }

    public Collection<LeaderboardGroup> getLeaderboardGroups() {
        return leaderboardGroups;
    }

    public Set<WindTrackMasterData> getWindTrackMasterData() {
        return windTrackMasterData;
    }

    public void setMasterDataExportFlagOnRaceColumns(boolean flagValue) {
        // collect all leaderboard groups for all events that will be touched during serialization
        final Set<LeaderboardGroup> allLeaderboardGroups = new HashSet<>();
        allLeaderboardGroups.addAll(leaderboardGroups);
        for (Entry<LeaderboardGroup, Set<Event>> i : eventForLeaderboardGroup.entrySet()) {
            for (Event e : i.getValue()) {
                Util.addAll(e.getLeaderboardGroups(), allLeaderboardGroups);
            }
        }
        for (LeaderboardGroup group : allLeaderboardGroups) {
            for (Leaderboard leaderboard : group.getLeaderboards()) {
                for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                    raceColumn.setMasterDataExportOngoingThreadFlag(true);
                }
            }
        }
    }

    public Map<RegattaIdentifier, Set<String>> getRaceIdStringsForRegatta() {
        return raceIdStringsForRegatta;
    }

    public Map<LeaderboardGroup, Set<Event>> getEventForLeaderboardGroup() {
        return eventForLeaderboardGroup;
    }

    public Iterable<Event> getAllEvents() {
        Map<UUID, Event> allEventsInMasterData = new HashMap<>();
        for (Set<Event> events : eventForLeaderboardGroup.values()) {
            for (Event e : events) {
                allEventsInMasterData.put(e.getId(), e);
            }
        }
        return allEventsInMasterData.values();
    }

    public Iterable<Regatta> getAllRegattas() {
        Set<Regatta> regattas = new HashSet<>();
        for (LeaderboardGroup leaderboardGroup : leaderboardGroups) {
            for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                if (leaderboard instanceof RegattaLeaderboard) {
                    RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
                    regattas.add(regattaLeaderboard.getRegatta());
                }
            }
        }
        return regattas;
    }

    /**
     * Copies only those media tracks from {@code allMediaTracks} to {@code filteredMediaTracks} which are 
     * assigned to races related to any of  exported {@link #leaderboardGroups}.
     */
    private void filterMediaTracks(Iterable<MediaTrack> allMediaTracks, Set<MediaTrack> filteredMediaTracks) {
        final Set<RaceIdentifier> raceIdentitifiersForMediaExport = collectRaceIdentifiersForMediaExport();
        for (MediaTrack mediaTrack : allMediaTracks) {
            for (RegattaAndRaceIdentifier raceIdentifier : mediaTrack.assignedRaces) {
                if (raceIdentitifiersForMediaExport.contains(raceIdentifier)) {
                    filteredMediaTracks.add(mediaTrack);
                }
            }
        }
    }

    /**
     * Returns the set of races (in the form of {@link RaceIdentifier}s) related to the exported
     * {@link #leaderboardGroups}.
     */
    private Set<RaceIdentifier> collectRaceIdentifiersForMediaExport() {
        final Set<RaceIdentifier> raceIdentifiers = new HashSet<>();
        for (LeaderboardGroup leaderboardGroup : leaderboardGroups) {
            for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                    for (Fleet fleet : raceColumn.getFleets()) {
                        RaceIdentifier raceIdentifier = raceColumn.getRaceIdentifier(fleet);
                        if (raceIdentifier != null) {
                            raceIdentifiers.add(raceIdentifier);
                        }
                    }
                }
            }
        }
        return raceIdentifiers;
    }

    public Map<DeviceIdentifier, Set<Timed>> getRaceLogTrackingFixes() {
        return raceLogTrackingFixes;
    }

    public Map<DeviceConfigurationMatcher, DeviceConfiguration> getDeviceConfigurations() {
        return deviceConfigurations;
    }
}
