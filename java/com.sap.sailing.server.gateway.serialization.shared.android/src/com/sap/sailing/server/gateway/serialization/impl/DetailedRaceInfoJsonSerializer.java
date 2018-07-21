package com.sap.sailing.server.gateway.serialization.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.anniversary.DetailedRaceInfo;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.dto.EventType;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class DetailedRaceInfoJsonSerializer
        implements JsonSerializer<DetailedRaceInfo>, JsonDeserializer<DetailedRaceInfo> {

    public static final String FIELD_EVENT_ID = "eventID";
    public static final String FIELD_EVENT_NAME = "eventName";
    public static final String FIELD_EVENT_TYPE = "eventType";
    public static final String FIELD_LEADERBOARD_NAME = "leaderboardName";
    public static final String FIELD_LEADERBOARD_DISPLAY_NAME = "leaderboardDisplayName";
    public static final String FIELD_REMOTEURL = "remoteUrl";
    public static final String FIELD_RACES = "races";

    @Override
    public JSONObject serialize(DetailedRaceInfo object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_EVENT_ID, object.getEventID().toString());
        result.put(FIELD_EVENT_NAME, object.getEventName());
        result.put(FIELD_EVENT_TYPE, object.getEventType() == null ? null : object.getEventType().toString());
        result.put(SimpleRaceInfoJsonSerializer.FIELD_RACE_NAME, object.getIdentifier().getRaceName());
        result.put(SimpleRaceInfoJsonSerializer.FIELD_REGATTA_NAME, object.getIdentifier().getRegattaName());
        result.put(FIELD_LEADERBOARD_NAME, object.getLeaderboardName());
        result.put(FIELD_LEADERBOARD_DISPLAY_NAME, object.getLeaderboardDisplayName());
        result.put(SimpleRaceInfoJsonSerializer.FIELD_START_OF_RACE, object.getStartOfRace().asMillis());
        final URL remoteUrl = object.getRemoteUrl();
        result.put(FIELD_REMOTEURL, remoteUrl == null ? null : remoteUrl.toExternalForm());
        return result;
    }

    @Override
    public DetailedRaceInfo deserialize(JSONObject object) throws JsonDeserializationException {
        String eventId = object.get(FIELD_EVENT_ID).toString();
        String raceName = object.get(SimpleRaceInfoJsonSerializer.FIELD_RACE_NAME).toString();
        String regattaName = object.get(SimpleRaceInfoJsonSerializer.FIELD_REGATTA_NAME).toString();
        String leaderboardName = object.get(FIELD_LEADERBOARD_NAME).toString();
        final Object leaderboardDisplayNameJson = object.get(FIELD_LEADERBOARD_DISPLAY_NAME);
        final String leaderboardDisplayName;
        if (leaderboardDisplayNameJson == null) {
            leaderboardDisplayName = null;
        } else {
            leaderboardDisplayName = leaderboardDisplayNameJson.toString();
        }
        final Object eventNameJson = object.get(FIELD_EVENT_NAME);
        final String eventName;
        if (eventNameJson == null) {
            eventName = null;
        } else {
            eventName = eventNameJson.toString();
        }
        final Object typeJson = object.get(FIELD_EVENT_TYPE);
        final EventType type;
        if (typeJson == null) {
            type = null;
        } else {
            type = EventType.valueOf(typeJson.toString());
        }
        TimePoint startOfRace = new MillisecondsTimePoint(
                ((Number) object.get(SimpleRaceInfoJsonSerializer.FIELD_START_OF_RACE)).longValue());
        String remoteUrl = (String) object.get(FIELD_REMOTEURL);
        URL remoteUrlObj = null;
        if (remoteUrl != null && !remoteUrl.isEmpty()) {
            try {
                remoteUrlObj = new URL(remoteUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return new DetailedRaceInfo(new RegattaNameAndRaceName(regattaName, raceName), leaderboardName,
                leaderboardDisplayName, startOfRace, UUID.fromString(eventId), eventName, type, remoteUrlObj);
    }
}