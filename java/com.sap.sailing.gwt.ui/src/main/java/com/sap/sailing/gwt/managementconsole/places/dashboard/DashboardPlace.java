package com.sap.sailing.gwt.managementconsole.places.dashboard;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsolePlace;

public class DashboardPlace extends AbstractManagementConsolePlace {

    @Prefix("dashboard")
    public static class Tokenizer extends AbstractManagementConsolePlace.DefautTokenizer<DashboardPlace> {

        public Tokenizer() {
            super(DashboardPlace::new);
        }

    }

}
