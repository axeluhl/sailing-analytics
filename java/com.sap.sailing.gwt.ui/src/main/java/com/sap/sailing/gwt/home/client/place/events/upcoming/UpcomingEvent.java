package com.sap.sailing.gwt.home.client.place.events.upcoming;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class UpcomingEvent extends Composite {

    @UiField SpanElement eventName;
    @UiField SpanElement seriesName;
    @UiField SpanElement venueName;
    @UiField SpanElement eventStartDate;
    @UiField Anchor eventOverviewLink;

    private final HomePlacesNavigator navigator;
    private final PlaceNavigation<EventPlace> eventNavigation; 

    interface UpcomingEventUiBinder extends UiBinder<Widget, UpcomingEvent> {
    }
    
    private static UpcomingEventUiBinder uiBinder = GWT.create(UpcomingEventUiBinder.class);

    public UpcomingEvent(final EventBaseDTO event, final HomePlacesNavigator navigator) {
        this.navigator = navigator;

        EventsOverviewUpcomingResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));

        eventNavigation = navigator.getEventNavigation(event.id.toString(), event.getBaseURL(), event.isOnRemoteServer());
        eventOverviewLink.setHref(eventNavigation.getTargetUrl());
        
        eventName.setInnerText(event.getName());
        venueName.setInnerText(event.venue.getName());
        eventStartDate.setInnerText(EventDatesFormatterUtil.formatDateRangeWithoutYear(event.startDate, event.endDate));
    }
    
    @UiHandler("eventOverviewLink")
    public void goToEventOverview(ClickEvent e) {
        navigator.goToPlace(eventNavigation);
        e.preventDefault();
    }

}
