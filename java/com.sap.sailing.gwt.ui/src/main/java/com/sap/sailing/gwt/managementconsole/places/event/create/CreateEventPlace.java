package com.sap.sailing.gwt.managementconsole.places.event.create;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsolePlace;

public class CreateEventPlace extends AbstractManagementConsolePlace {

    @Prefix("create-event")
    public static class Tokenizer extends AbstractManagementConsolePlace.DefautTokenizer<CreateEventPlace> {

        public Tokenizer() {
            super(CreateEventPlace::new);
        }

    }

}
