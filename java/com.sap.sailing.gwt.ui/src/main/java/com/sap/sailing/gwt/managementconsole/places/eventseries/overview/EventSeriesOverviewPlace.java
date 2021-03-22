package com.sap.sailing.gwt.managementconsole.places.eventseries.overview;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsolePlace;

public class EventSeriesOverviewPlace extends AbstractManagementConsolePlace {

    @Prefix("eventseries/overview")
    public static class Tokenizer extends AbstractManagementConsolePlace.DefautTokenizer<EventSeriesOverviewPlace> {

        public Tokenizer() {
            super(EventSeriesOverviewPlace::new);
        }

    }

}
