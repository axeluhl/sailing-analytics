package com.sap.sailing.gwt.home.client.shared.recentevent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventPlace.Tokenizer;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class RecentEvent extends Composite {
    
    @UiField SpanElement eventName;
    @UiField SpanElement venueName;
    @UiField SpanElement eventStartDate;
    @UiField Anchor eventOverviewLink;
    @UiField ImageElement eventImage;

    private EventBaseDTO event;

    private final String defaultImageUrl = "http://static.sapsailing.com/newhome/default_event_photo.jpg";

    interface RecentEventUiBinder extends UiBinder<Widget, RecentEvent> {
    }
    
    private static RecentEventUiBinder uiBinder = GWT.create(RecentEventUiBinder.class);

    private final PlaceNavigator navigator;
    
    public RecentEvent(PlaceNavigator navigator) {
        this.navigator = navigator;
        RecentEventResources.INSTANCE.css().ensureInjected();

        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void setEvent(EventBaseDTO event) {
        this.event = event;
        updateUI();
    }

    @UiHandler("eventOverviewLink")
    public void goToEventPlace(ClickEvent e) {
        if(event.getBaseURL().contains("localhost") || event.getBaseURL().contains("127.0.0.1")) {
            navigator.goToEvent(event.id.toString());
        } else {
            EventPlace eventPlace = new EventPlace(event.id.toString());
            EventPlace.Tokenizer t = new Tokenizer();
            String remoteEventUrl = event.getBaseURL() + "/gwt/Home.html#" + EventPlace.class.getSimpleName() + ":" + t.getToken(eventPlace);
            Window.Location.replace(remoteEventUrl);
        }
    }

    private void updateUI() {
        eventName.setInnerText(event.getName());
        venueName.setInnerText(event.venue.getName());
        eventStartDate.setInnerText(EventDatesFormatterUtil.formatDateRangeWithoutYear(event.startDate, event.endDate));
        
        if(event.getImageURLs().size() == 0) {
            eventImage.setSrc(defaultImageUrl);
        } else {
            eventImage.setSrc(event.getImageURLs().get(0));
        }
    }
}
