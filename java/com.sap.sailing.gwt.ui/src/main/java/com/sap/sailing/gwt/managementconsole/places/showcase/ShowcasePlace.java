package com.sap.sailing.gwt.managementconsole.places.showcase;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsolePlace;

public class ShowcasePlace extends AbstractManagementConsolePlace {

    @Prefix("showcase")
    public static class Tokenizer extends AbstractManagementConsolePlace.DefautTokenizer<ShowcasePlace> {

        public Tokenizer() {
            super(ShowcasePlace::new);
        }

    }

}
