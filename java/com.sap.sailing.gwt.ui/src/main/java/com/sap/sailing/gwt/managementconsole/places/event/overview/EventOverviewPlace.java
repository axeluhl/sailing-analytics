package com.sap.sailing.gwt.managementconsole.places.event.overview;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsolePlace;

public class EventOverviewPlace extends AbstractManagementConsolePlace {

    @Prefix("event/overview")
    public static class Tokenizer extends AbstractManagementConsolePlace.DefautTokenizer<EventOverviewPlace> {

        public Tokenizer() {
            super(EventOverviewPlace::new);
        }
    }

}
