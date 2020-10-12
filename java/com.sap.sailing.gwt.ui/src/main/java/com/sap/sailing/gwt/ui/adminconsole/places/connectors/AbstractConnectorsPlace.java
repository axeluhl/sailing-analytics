package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractAdminConsolePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class AbstractConnectorsPlace extends AbstractAdminConsolePlace {

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.CONNECTORS;
    }

}
