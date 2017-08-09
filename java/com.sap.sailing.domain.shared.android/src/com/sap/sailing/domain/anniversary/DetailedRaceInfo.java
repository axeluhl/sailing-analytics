package com.sap.sailing.domain.anniversary;

import java.net.URL;
import java.util.UUID;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sse.common.TimePoint;

/**
 * Unused class that is meant to store found anniversaries, currently not used as only a simpler version of bug4227 is implemented yet.
 *
 */
public class DetailedRaceInfo extends SimpleRaceInfo {
    private final String leaderboardName;
    private final UUID eventID;
    
    public DetailedRaceInfo(RegattaAndRaceIdentifier identifier, String leaderboardName, TimePoint timePoint,
            UUID eventId, URL remoteUrl) {
        super(identifier, timePoint,remoteUrl);
        if (leaderboardName == null || eventId == null) {
            throw new IllegalStateException("DetailedRaceInfo Data is not allowed to contain any null values!");
        }
        this.leaderboardName = leaderboardName;
        this.eventID = eventId;
    }

    /**
     * Copy constructor to easily switch remoteUrl value 
     */
    public DetailedRaceInfo(DetailedRaceInfo copy, URL remoteUrl) {
        this(copy.getIdentifier(), copy.getLeaderboardName(), copy.getStartOfRace(), copy.getEventID(), remoteUrl);
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
}
