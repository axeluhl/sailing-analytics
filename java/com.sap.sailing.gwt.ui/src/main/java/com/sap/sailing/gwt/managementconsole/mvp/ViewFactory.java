package com.sap.sailing.gwt.managementconsole.mvp;

import com.sap.sailing.gwt.managementconsole.partials.authentication.signin.SignInView;
import com.sap.sailing.gwt.managementconsole.partials.authentication.signin.SignInViewImpl;
import com.sap.sailing.gwt.managementconsole.places.event.media.EventMediaView;
import com.sap.sailing.gwt.managementconsole.places.event.media.EventMediaViewImpl;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewView;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewViewImpl;

public class ViewFactory {

    private EventOverviewView eventOverviewView;

    private EventMediaView eventMediaView;

    public EventOverviewView getEventOverviewView() {
        if (eventOverviewView == null) {
            eventOverviewView = new EventOverviewViewImpl();
        }
        return eventOverviewView;
    }

    public EventMediaView getEventMediaView() {
        if (eventMediaView == null) {
            eventMediaView = new EventMediaViewImpl();
        }
        return eventMediaView;
    }

    public SignInView getSignInView() {
        return new SignInViewImpl();
    }

}
