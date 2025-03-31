package com.sap.sailing.gwt.managementconsole.places;

import com.google.gwt.activity.shared.AbstractActivity;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;

public abstract class AbstractManagementConsoleActivity<P extends AbstractManagementConsolePlace>
        extends AbstractActivity {

    private final ManagementConsoleClientFactory clientFactory;
    private final P place;

    protected AbstractManagementConsoleActivity(final ManagementConsoleClientFactory clientFactory, final P place) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    protected ManagementConsoleClientFactory getClientFactory() {
        return clientFactory;
    }

    protected P getPlace() {
        return place;
    }

}
