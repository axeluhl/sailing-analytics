package com.sap.sailing.gwt.managementconsole.places.regatta.overview;

import java.util.UUID;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsolePlace;

public class RegattaOverviewPlace extends AbstractManagementConsolePlace {

    private final UUID eventId;

    public RegattaOverviewPlace(final UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getEventId() {
        return eventId;
    }
    
    @Prefix("regatta/overview")
    public static class Tokenizer extends AbstractManagementConsolePlace.UUIDTokenizer<RegattaOverviewPlace> {
        public Tokenizer() {
            super(RegattaOverviewPlace::new, RegattaOverviewPlace::getEventId);
        }
    }

}
