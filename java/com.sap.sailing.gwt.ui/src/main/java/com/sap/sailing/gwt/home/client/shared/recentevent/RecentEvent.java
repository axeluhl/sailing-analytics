package com.sap.sailing.gwt.home.client.shared.recentevent;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class RecentEvent extends Composite {
    
    @UiField SpanElement eventName;
    @UiField SpanElement venueName;
    @UiField SpanElement eventStartDate;
    @UiField AnchorElement eventOverviewLink;
    @UiField ImageElement eventImage;

    private EventBaseDTO event;

    private final String defaultImageUrl = "http://static.sapsailing.com/ubilabsimages/default/default_event_photo.jpg";

    interface RecentEventUiBinder extends UiBinder<Widget, RecentEvent> {
    }
    
    private static RecentEventUiBinder uiBinder = GWT.create(RecentEventUiBinder.class);

    public RecentEvent(final PlaceNavigator navigator) {
        RecentEventResources.INSTANCE.css().ensureInjected();

        initWidget(uiBinder.createAndBindUi(this));
        
        Event.sinkEvents(eventOverviewLink, Event.ONCLICK);
        Event.setEventListener(eventOverviewLink, new EventListener() {
            @Override
            public void onBrowserEvent(Event browserEvent) {
                navigator.goToEvent(event.id.toString(), event.getBaseURL(), event.isOnRemoteServer());
            }
        });

    }
    
    public void setEvent(EventBaseDTO event) {
        this.event = event;
        updateUI();
    }
    
    private void updateUI() {
        eventName.setInnerText(event.getName());
        venueName.setInnerText(event.venue.getName());
        eventStartDate.setInnerText(EventDatesFormatterUtil.formatDateRangeWithoutYear(event.startDate, event.endDate));
        
        List<String> photoGalleryImageURLs = event.getPhotoGalleryImageURLs();
        if(photoGalleryImageURLs.size() == 0) {
            eventImage.setSrc(defaultImageUrl);
        } else {
            eventImage.setSrc(photoGalleryImageURLs.get(0));
        }
    }
}
