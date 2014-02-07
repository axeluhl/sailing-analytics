package com.sap.sailing.server.gateway.masterdata;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.masterdataimport.WindTrackMasterData;
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
    private final Map<String, Regatta> regattaForRaceIdStrings;
    private final Set<MediaTrack> allMediaTracks;
    private final Set<LeaderboardGroup> leaderboardGroups;
    private final Set<WindTrackMasterData> windTrackMasterData;

    public TopLevelMasterData(final Set<LeaderboardGroup> groupsToExport, final Iterable<Event> allEvents,
            final Map<String, Regatta> regattaForRaceIdString, final Collection<MediaTrack> allMediaTracks) {
        this.regattaForRaceIdStrings = regattaForRaceIdString;
        this.allMediaTracks = new HashSet<MediaTrack>();
        this.allMediaTracks.addAll(allMediaTracks);
        this.leaderboardGroups = groupsToExport;
        this.windTrackMasterData = fillWindMap(groupsToExport);
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

    public Map<String, Regatta> getRegattaForRaceIdStrings() {
        return regattaForRaceIdStrings;
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

}
