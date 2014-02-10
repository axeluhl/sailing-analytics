package com.sap.sailing.domain.masterdataimport;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;

/**
 * Holds all information needed for a master data import.
 * 
 * @author Frederik Petersen (D054528)
 * 
 */
public class TopLevelMasterData implements Serializable {

    private static final long serialVersionUID = 4820893865792553281L;
    private final Map<Regatta, Set<String>> raceIdStringsForRegatta;
    private final Set<MediaTrack> allMediaTracks;
    private final Set<LeaderboardGroup> leaderboardGroups;
    private final Set<WindTrackMasterData> windTrackMasterData;
    private final Map<LeaderboardGroup, Set<Event>> eventForLeaderboardGroup;

    public TopLevelMasterData(final Set<LeaderboardGroup> groupsToExport, final Iterable<Event> allEvents,
            final Map<String, Regatta> regattaForRaceIdString, final Collection<MediaTrack> allMediaTracks) {
        this.raceIdStringsForRegatta = convertToRaceIdStringsForRegattaMap(regattaForRaceIdString);
        this.allMediaTracks = new HashSet<MediaTrack>();
        this.allMediaTracks.addAll(allMediaTracks);
        this.leaderboardGroups = groupsToExport;
        this.windTrackMasterData = fillWindMap(groupsToExport);
        this.eventForLeaderboardGroup = createEventMap(groupsToExport, allEvents);
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
                if (leaderboard instanceof RegattaLeaderboard) {
                    RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
                    CourseArea courseArea = regattaLeaderboard.getRegatta().getDefaultCourseArea();
                    if (courseArea != null) {
                        Event event = eventForCourseArea.get(courseArea);
                        if (event != null) {
                            eventSet.add(event);
                        }
                    }
                }
            }

        }
        return eventsForLeaderboardGroup;
    }

    private Map<Regatta, Set<String>> convertToRaceIdStringsForRegattaMap(Map<String, Regatta> regattaForRaceIdString) {
        Map<Regatta, Set<String>> raceIdStringsForRegatta = new HashMap<Regatta, Set<String>>();
        for (Entry<String, Regatta> entry : regattaForRaceIdString.entrySet()) {
            Regatta regatta = entry.getValue();
            Set<String> raceIds = raceIdStringsForRegatta.get(regatta);
            if (raceIds == null) {
                raceIds = new HashSet<String>();
                raceIdStringsForRegatta.put(regatta, raceIds);
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
                        WindTrack track = trackedRace.getOrCreateWindTrack(source);
                        track.lockForRead();
                        try {
                            windTrackMasterDataSet.add(new WindTrackMasterData(source.getType(), source.getId(), track
                                .getFixes(), regattaName, raceName, raceId));
                        } finally {
                            track.unlockAfterRead();
                        }
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

    public Map<Regatta, Set<String>> getRaceIdStringsForRegatta() {
        return raceIdStringsForRegatta;
    }

    public Map<LeaderboardGroup, Set<Event>> getEventForLeaderboardGroup() {
        return eventForLeaderboardGroup;
    }

}
