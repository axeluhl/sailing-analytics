package com.sap.sailing.domain.anniversary;

import java.net.URL;
import java.util.UUID;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.EventType;
import com.sap.sse.common.TimePoint;

/**
 * Reference to an "anniversary" race, such that it can be used to obtain a link to the race on its hosting server. For
 * this, the object hosts additional information about the leaderboard and the event.
 * Object creation will fail with an exception if no leaderboardName and eventId is given. All other data is optional.
 */
public class DetailedRaceInfo extends SimpleRaceInfo {
    private static final long serialVersionUID = 1L;
    private final String eventName;
    private final String leaderboardDisplayName;
    private final String leaderboardName;
    private final UUID eventID;
    private EventType eventType;

    public DetailedRaceInfo(RegattaAndRaceIdentifier identifier, String leaderboardName, String leaderboardDisplayName,
            TimePoint timePoint, UUID eventId, String eventName, EventType eventType,
            URL remoteUrl) {
        super(identifier, timePoint, remoteUrl);
        if (leaderboardName == null || eventId == null) {
            throw new IllegalStateException("DetailedRaceInfo Data is not allowed to contain any null values!");
        }
        this.leaderboardName = leaderboardName;
        this.leaderboardDisplayName = leaderboardDisplayName;
        this.eventName = eventName;
        this.eventID = eventId;
        this.eventType = eventType;
    }

    /**
     * Copy constructor to easily switch remoteUrl value
     */
    public DetailedRaceInfo(DetailedRaceInfo copy, URL remoteUrl) {
        this(copy.getIdentifier(), copy.getLeaderboardName(), copy.getLeaderboardDisplayName(), copy.getStartOfRace(),
                copy.getEventID(), copy.getEventName(), copy.getEventType(), remoteUrl);
    }

    public String getLeaderboardDisplayName() {
        return leaderboardDisplayName;
    }

    public String getEventName() {
        return eventName;
    }

    public String getLeaderboardName() {
        return leaderboardName;
    }

    public UUID getEventID() {
        return eventID;
    }

    @Override
    public String toString() {
        return "DetailedRaceInfo [identifier=" + getIdentifier() + ", leaderboardName=" + leaderboardName
                + ", startOfRace=" + getStartOfRace() + ", eventID=" + eventID + "]";
    }

    public EventType getEventType() {
        return eventType;
    }
}
