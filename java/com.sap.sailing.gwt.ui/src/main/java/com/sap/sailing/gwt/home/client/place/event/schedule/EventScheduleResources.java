package com.sap.sailing.gwt.home.client.place.event.schedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface EventScheduleResources extends ClientBundle {
    public static final EventScheduleResources INSTANCE = GWT.create(EventScheduleResources.class);

    @Source("com/sap/sailing/gwt/home/client/place/event/schedule/EventSchedule.css")
    LocalCss css();

    public interface LocalCss extends CssResource {

    }
}
