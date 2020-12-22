package com.sap.sailing.gwt.managementconsole.mvp;

import com.sap.sailing.gwt.managementconsole.partials.authentication.signin.SignInView;
import com.sap.sailing.gwt.managementconsole.partials.authentication.signin.SignInViewImpl;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewView;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewViewImpl;

public class ViewFactory {

    private EventOverviewView eventOverviewView;

    public EventOverviewView getEventOverviewView() {
        if (eventOverviewView == null) {
            eventOverviewView = new EventOverviewViewImpl();
        }
        return eventOverviewView;
    }

    public SignInView getSignInView() {
        return new SignInViewImpl();
    }

}
