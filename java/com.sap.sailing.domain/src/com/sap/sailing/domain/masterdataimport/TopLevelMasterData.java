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
import com.sap.sailing.domain.abstractlog.shared.events.DeviceMappingEvent;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.impl.DeviceMappingImpl;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixTrackImpl;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.WithID;
import com.sap.sse.common.impl.TimeRangeImpl;

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
    private final Set<MediaTrack> allMediaTracks;
    private final Set<LeaderboardGroup> leaderboardGroups;
    private final Set<WindTrackMasterData> windTrackMasterData;
    private final Map<LeaderboardGroup, Set<Event>> eventForLeaderboardGroup;
    private final Map<DeviceIdentifier, Set<GPSFix>> raceLogTrackingFixes;

    public TopLevelMasterData(final Set<LeaderboardGroup> groupsToExport, final Iterable<Event> allEvents,
            final Map<String, Regatta> regattaForRaceIdString, final Collection<MediaTrack> allMediaTracks,
            GPSFixStore gpsFixStore, boolean exportWind) {
        this.raceIdStringsForRegatta = convertToRaceIdStringsForRegattaMap(regattaForRaceIdString);
        this.allMediaTracks = new HashSet<MediaTrack>();
        this.allMediaTracks.addAll(allMediaTracks);
        this.leaderboardGroups = groupsToExport;
        this.raceLogTrackingFixes = getAllRelevantRaceLogTrackingFixes(gpsFixStore);
        if (exportWind) {
            this.windTrackMasterData = fillWindMap(groupsToExport);
        } else {
            this.windTrackMasterData = new HashSet<WindTrackMasterData>();
        }
        this.eventForLeaderboardGroup = createEventMap(groupsToExport, allEvents);
    }

    private Map<DeviceIdentifier, Set<GPSFix>> getAllRelevantRaceLogTrackingFixes(GPSFixStore gpsFixStore) {
        Map<DeviceIdentifier, Set<GPSFix>> relevantFixes = new HashMap<>();
        
        //Add fixes for regatta log mappings
        for (Regatta regatta : getAllRegattas()) {
            RegattaLog regattaLog = regatta.getRegattaLog();
            try {
                regattaLog.lockForRead();
                for (RegattaLogEvent logEvent : regattaLog.getRawFixes()) {
                    addAllFixesIfMappingEvent(gpsFixStore, relevantFixes, logEvent);
                }
            } finally {
                regattaLog.unlockAfterRead();
            }
        }
        //Add fixes for race log mapping
        for (LeaderboardGroup group : leaderboardGroups) {
            for (Leaderboard leaderboard : group.getLeaderboards()) {
                for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                    for (Fleet fleet : raceColumn.getFleets()) {
                        RaceLog raceLog = raceColumn.getRaceLog(fleet);
                        try {
                            raceLog.lockForRead();
                            for (RaceLogEvent logEvent : raceLog.getRawFixes()) {
                                addAllFixesIfMappingEvent(gpsFixStore, relevantFixes, logEvent);
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

    private void addAllFixesIfMappingEvent(GPSFixStore gpsFixStore, Map<DeviceIdentifier, Set<GPSFix>> relevantFixes,
            AbstractLogEvent<?> logEvent) {
        if (logEvent instanceof DeviceMappingEvent<?,?>) {
            DeviceMappingEvent<?,?> mappingEvent = (DeviceMappingEvent<?,?>) logEvent;
            try {
                addAllFixesForMappingEvent(gpsFixStore, relevantFixes, mappingEvent);
            } catch (NoCorrespondingServiceRegisteredException | TransformationException e) {
                logger.severe("Failed to add fixes to exportdata for mapping Event");
                e.printStackTrace();
            }
        }
    }

    private void addAllFixesForMappingEvent(GPSFixStore gpsFixStore, Map<DeviceIdentifier, Set<GPSFix>> relevantFixes,
            DeviceMappingEvent<?, ?> mappingEvent) throws NoCorrespondingServiceRegisteredException, TransformationException {
        DynamicGPSFixTrack<WithID, GPSFix> track = new DynamicGPSFixTrackImpl<WithID>(mappingEvent.getMappedTo(), 10000);
        DeviceIdentifier device = mappingEvent.getDevice();
        gpsFixStore.loadTrack(track, new DeviceMappingImpl<WithID>(mappingEvent.getMappedTo(), device,
                new TimeRangeImpl(mappingEvent.getFrom(), mappingEvent.getTo())));
        if (!relevantFixes.containsKey(device)) {
            relevantFixes.put(device, new HashSet<>());
        }
        Set<GPSFix> fixes = relevantFixes.get(device);
        try {
            track.lockForRead();
            for (GPSFix fix : track.getRawFixes()) {
                fixes.add(fix);
            }
        } finally {
            track.unlockAfterRead();
        }
    }

    /**
     * Workaround to look for the events connected to RegattaLeadeboards. There should be a proper connection between
     * regatta and event soon. TODO
     * 
     * @param groupsToExport
     * @param allEvents
     * @return
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

    public Collection<MediaTrack> getAllMediaTracks() {
        return allMediaTracks;
    }

    public Collection<LeaderboardGroup> getLeaderboardGroups() {
        return leaderboardGroups;
    }

    public Set<WindTrackMasterData> getWindTrackMasterData() {
        return windTrackMasterData;
    }

    public void setMasterDataExportFlagOnRaceColumns(boolean flagValue) {
        for (LeaderboardGroup group : leaderboardGroups) {
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

    public Map<DeviceIdentifier, Set<GPSFix>> getRaceLogTrackingFixes() {
        return raceLogTrackingFixes;
    }
}
