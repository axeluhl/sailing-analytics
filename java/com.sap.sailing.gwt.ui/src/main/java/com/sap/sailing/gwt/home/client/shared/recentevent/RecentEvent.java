package com.sap.sailing.gwt.home.client.shared.recentevent;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.home.client.shared.LongNamesUtil;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class RecentEvent extends UIObject {
    
    @UiField SpanElement eventName;
    @UiField SpanElement venueName;
    @UiField SpanElement eventStartDate;
    @UiField AnchorElement eventOverviewLink;
    @UiField ImageElement eventImage;
    @UiField DivElement isLiveDiv;
    
    private final EventBaseDTO event;

    interface RecentEventUiBinder extends UiBinder<DivElement, RecentEvent> {
    }
    
    private static RecentEventUiBinder uiBinder = GWT.create(RecentEventUiBinder.class);

    public RecentEvent(final PlaceNavigator navigator, final EventBaseDTO event) {
        this.event = event;
        RecentEventResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
        
        final PlaceNavigation<EventPlace> eventNavigation = navigator.getEventNavigation(event.id.toString(), event.getBaseURL(), event.isOnRemoteServer());
        eventOverviewLink.setHref(eventNavigation.getTargetUrl());
        
        Event.sinkEvents(eventOverviewLink, Event.ONCLICK);
        Event.setEventListener(eventOverviewLink, new EventListener() {
            @Override
            public void onBrowserEvent(Event browserEvent) {
                switch (DOM.eventGetType(browserEvent)) {
                    case Event.ONCLICK:
                        navigator.goToPlace(eventNavigation);
                        break;
                }
            }
        });
        updateUI();
    }
    
    private void updateUI() {
        SafeHtml safeHtmlEventName = LongNamesUtil.breakLongName(event.getName());
        eventName.setInnerSafeHtml(safeHtmlEventName);
        if (!event.isRunning()) {
            isLiveDiv.getStyle().setDisplay(Display.NONE);
        }
        venueName.setInnerText(event.venue.getName());
        eventStartDate.setInnerText(EventDatesFormatterUtil.formatDateRangeWithoutYear(event.startDate, event.endDate));
        List<String> photoGalleryImageURLs = event.getPhotoGalleryImageURLs();
        if (photoGalleryImageURLs.isEmpty()) {
            eventImage.setSrc(RecentEventResources.INSTANCE.defaultEventPhotoImage().getSafeUri().asString());
        } else {
            eventImage.setSrc(photoGalleryImageURLs.get(0));
        }
    }
}
