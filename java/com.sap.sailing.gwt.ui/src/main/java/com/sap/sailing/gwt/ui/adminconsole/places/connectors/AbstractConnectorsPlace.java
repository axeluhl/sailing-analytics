package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractAdminConsolePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public abstract class AbstractConnectorsPlace extends AbstractAdminConsolePlace {
    public AbstractConnectorsPlace(String token) {
        super(token);
    }

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.CONNECTORS;
    }
}
