package com.sap.sailing.gwt.managementconsole.places;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;

/**
 * Abstract super class for management console {@link Activity activity} implementations holding instances of the
 * application's {@link ManagementConsoleClientFactory client factory} and the {@link AbstractManagementConsolePlace
 * place} it is mapped from.
 *
 * @param <P>
 *            the actual {@link AbstractManagementConsolePlace} sub-type
 */
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
