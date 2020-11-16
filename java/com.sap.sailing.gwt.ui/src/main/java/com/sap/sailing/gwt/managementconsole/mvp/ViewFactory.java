package com.sap.sailing.gwt.managementconsole.mvp;

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

}
