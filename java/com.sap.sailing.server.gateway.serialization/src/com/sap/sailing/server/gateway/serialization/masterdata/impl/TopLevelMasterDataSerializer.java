package com.sap.sailing.server.gateway.serialization.masterdata.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

/**
 * Will take care of serializing all master data connected to a set of leaderboard groups. They
 * are identified in {@link #serialize(Set)}. The serializer must be created with a reference to all
 * leaderboard groups and all events, so that all necessary data can be found and serialized.
 * 
 * @author Frederik Petersen (D054528)
 *
 */
public class TopLevelMasterDataSerializer  {
    
    public static final String FIELD_PER_LG = "perLg";
    public static final String FIELD_MEDIA = "media";
    private final Map<String, LeaderboardGroup> allLeaderboardGroups;
    private final Iterable<Event> allEvents;
    private final ConcurrentHashMap<String, Regatta> regattaForRaceIdStrings;
    private final Collection<MediaTrack> allMediaTracks;
    
    private final JsonSerializer<MediaTrack> mediaTrackSerializer;

    public TopLevelMasterDataSerializer(Map<String, LeaderboardGroup> allLeaderboardGroups, Iterable<Event> allEvents,
            ConcurrentHashMap<String, Regatta> regattaForRaceIdString, Collection<MediaTrack> allMediaTracks) {
        this.allLeaderboardGroups = allLeaderboardGroups;
        this.allEvents = allEvents;
        this.regattaForRaceIdStrings = regattaForRaceIdString;
        this.allMediaTracks = allMediaTracks;
        mediaTrackSerializer = new MediaTrackJsonSerializer();
    }

    public JSONObject serialize(Set<String> requestedLeaderboardGroupNames) {
        JSONObject masterData = new JSONObject();
        
        JSONArray masterDataPerLg = new JSONArray();

        for (String name : requestedLeaderboardGroupNames) {
            LeaderboardGroup leaderboardGroup = allLeaderboardGroups.get(name);
            if (leaderboardGroup == null) {
                continue;
            }
            JsonSerializer<LeaderboardGroup> serializer = new LeaderboardGroupMasterDataJsonSerializer(allEvents, regattaForRaceIdStrings);
            masterDataPerLg.add(serializer.serialize(leaderboardGroup));
        }
        masterData.put(FIELD_PER_LG, masterDataPerLg);
        masterData.put(FIELD_MEDIA, createJsonArrayForMediaTracks());
        
        return masterData;
    }

    private JSONArray createJsonArrayForMediaTracks() {
        JSONArray array = new JSONArray();
        for (MediaTrack mediaTrack : allMediaTracks) {
            array.add(mediaTrackSerializer.serialize(mediaTrack));
        }
        return array;
    }

}
