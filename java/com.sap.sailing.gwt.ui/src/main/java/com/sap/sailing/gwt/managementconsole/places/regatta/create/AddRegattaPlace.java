package com.sap.sailing.gwt.managementconsole.places.regatta.create;

import java.util.UUID;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsolePlace;

public class AddRegattaPlace extends AbstractManagementConsolePlace {

    private final UUID eventId;
    
    public AddRegattaPlace(UUID eventId) {
        this.eventId = eventId;
    }
    
    public UUID getEventId() {
        return eventId;
    }
    
    @Prefix("add-regatta")
    public static class Tokenizer extends AbstractManagementConsolePlace.UUIDTokenizer<AddRegattaPlace> {
        public Tokenizer() {
            super(AddRegattaPlace::new, AddRegattaPlace::getEventId);
        }
    }

}
