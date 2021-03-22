package com.sap.sailing.gwt.managementconsole.places.eventseries.events;

import java.util.UUID;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsolePlace;

public class EventSeriesEventsPlace extends AbstractManagementConsolePlace {

    private final UUID leaderboardGroupId;

    public EventSeriesEventsPlace(final UUID leaderboardGroupId) {
        this.leaderboardGroupId = leaderboardGroupId;
    }

    public UUID getLeaderboardGroupId() {
        return leaderboardGroupId;
    }

    @Prefix("eventseries/events")
    public static class Tokenizer extends AbstractManagementConsolePlace.UUIDTokenizer<EventSeriesEventsPlace> {

        public Tokenizer() {
            super(EventSeriesEventsPlace::new, EventSeriesEventsPlace::getLeaderboardGroupId);
        }

    }

}
