package com.sap.sailing.gwt.managementconsole.mvp;

import com.sap.sailing.gwt.managementconsole.partials.authentication.signin.SignInView;
import com.sap.sailing.gwt.managementconsole.partials.authentication.signin.SignInViewImpl;
import com.sap.sailing.gwt.managementconsole.places.dashboard.DashboardView;
import com.sap.sailing.gwt.managementconsole.places.dashboard.DashboardViewImpl;
import com.sap.sailing.gwt.managementconsole.places.event.media.EventMediaView;
import com.sap.sailing.gwt.managementconsole.places.event.media.EventMediaViewImpl;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewView;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewViewImpl;
import com.sap.sailing.gwt.managementconsole.places.eventseries.events.EventSeriesEventsView;
import com.sap.sailing.gwt.managementconsole.places.eventseries.events.EventSeriesEventsViewImpl;
import com.sap.sailing.gwt.managementconsole.places.eventseries.overview.EventSeriesOverviewView;
import com.sap.sailing.gwt.managementconsole.places.eventseries.overview.EventSeriesOverviewViewImpl;
import com.sap.sailing.gwt.managementconsole.places.regatta.overview.RegattaOverviewView;
import com.sap.sailing.gwt.managementconsole.places.regatta.overview.RegattaOverviewViewImpl;

public class ViewFactory {
    private EventSeriesOverviewView eventSeriesOverviewView;
    private EventOverviewView eventOverviewView;
    private RegattaOverviewView regattaOverviewView;
    private EventMediaView eventMediaView;
    private EventSeriesEventsView eventSeriesEventsView;

    public DashboardView getDashboardView() {
        return new DashboardViewImpl();
    }

    public EventSeriesOverviewView getEventSeriesOverviewView() {
        if (eventSeriesOverviewView == null) {
            eventSeriesOverviewView = new EventSeriesOverviewViewImpl();
        }
        return eventSeriesOverviewView;
    }

    public EventSeriesEventsView getEventSeriesEventsView() {
        if (eventSeriesEventsView == null) {
            eventSeriesEventsView = new EventSeriesEventsViewImpl();
        }
        return eventSeriesEventsView;
    }

    public EventOverviewView getEventOverviewView() {
        if (eventOverviewView == null) {
            eventOverviewView = new EventOverviewViewImpl();
        }
        return eventOverviewView;
    }

    public RegattaOverviewView getRegattaOverviewView() {
        if (regattaOverviewView == null) {
            regattaOverviewView = new RegattaOverviewViewImpl();
        }
        return regattaOverviewView;
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
