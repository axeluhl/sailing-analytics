package com.sap.sailing.gwt.managementconsole.places.event.media;

import java.util.UUID;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsolePlace;

public class EventMediaPlace extends AbstractManagementConsolePlace {

    private final UUID eventId;

    public EventMediaPlace(final UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getEventId() {
        return eventId;
    }

    @Prefix("event/media")
    public static class Tokenizer extends AbstractManagementConsolePlace.UUIDTokenizer<EventMediaPlace> {

        public Tokenizer() {
            super(EventMediaPlace::new, EventMediaPlace::getEventId);
        }

    }

}
