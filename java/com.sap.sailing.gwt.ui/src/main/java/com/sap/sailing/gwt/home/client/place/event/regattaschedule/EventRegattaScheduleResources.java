package com.sap.sailing.gwt.home.client.place.event.regattaschedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface EventRegattaScheduleResources extends ClientBundle {
    public static final EventRegattaScheduleResources INSTANCE = GWT.create(EventRegattaScheduleResources.class);

    @Source("com/sap/sailing/gwt/home/client/place/event/regattaschedule/EventRegattaSchedule.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String eventregattaschedule();
        String eventregattaschedule_series();
        String eventregattaschedule_series_name();
        String eventregattaschedule_series_date();
        String eventregattaschedule_series_fleet();
        String eventregattaschedule_series_fleet_name();
        String eventregattaschedule_series_fleet_race();
        String eventregattaschedule_series_fleet_racetracked();
        String eventregattaschedule_series_fleet_race_name();
        String eventregattaschedule_series_fleet_raceuntracked();
        String eventregattaschedule_series_fleet_race_details();
    }
}
