package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleActivity;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsolePlace;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class AdminConsoleActivityProxy extends AbstractActivityProxy {

    private final AdminConsolePlace place;
    private final AdminConsoleClientFactory clientFactory;

    public AdminConsoleActivityProxy(AdminConsolePlace place, AdminConsoleClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new AdminConsoleActivity(place, clientFactory));
            }
        });
    }
}
