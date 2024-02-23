package com.sap.sailing.gwt.managementconsole.places.eventseries.create;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsolePlace;

public class CreateEventSeriesPlace extends AbstractManagementConsolePlace {

    @Prefix("create-event-series")
    public static class Tokenizer extends AbstractManagementConsolePlace.DefautTokenizer<CreateEventSeriesPlace> {

        public Tokenizer() {
            super(CreateEventSeriesPlace::new);
        }

    }

}
