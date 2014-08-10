package com.sap.sailing.gwt.home.client.place.events.upcoming;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class UpcomingEvent extends UIObject {

    @UiField SpanElement eventName;
    @UiField SpanElement seriesName;
    @UiField SpanElement venueName;
    @UiField SpanElement eventStartDate;
    @UiField AnchorElement eventOverviewLink;

    @SuppressWarnings("unused")
    private final PlaceNavigator navigator;

    interface UpcomingEventUiBinder extends UiBinder<DivElement, UpcomingEvent> {
    }
    
    private static UpcomingEventUiBinder uiBinder = GWT.create(UpcomingEventUiBinder.class);

    public UpcomingEvent(final EventBaseDTO event, final PlaceNavigator navigator) {
        this.navigator = navigator;

        EventsOverviewUpcomingResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
        
        Event.sinkEvents(eventOverviewLink, Event.ONCLICK);
        Event.setEventListener(eventOverviewLink, new EventListener() {
            @Override
            public void onBrowserEvent(Event browserEvent) {
                switch (DOM.eventGetType(browserEvent)) {
                    case Event.ONCLICK:
                        navigator.goToEvent(event.id.toString(), event.getBaseURL(), event.isOnRemoteServer());
                        break;
                }
            }
        });

        eventName.setInnerText(event.getName());
        venueName.setInnerText(event.venue.getName());
        eventStartDate.setInnerText(EventDatesFormatterUtil.formatDateRangeWithoutYear(event.startDate, event.endDate));
    }
}
