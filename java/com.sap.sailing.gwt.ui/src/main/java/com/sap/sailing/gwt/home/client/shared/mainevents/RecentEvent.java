package com.sap.sailing.gwt.home.client.shared.mainevents;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class RecentEvent extends Composite {
    
    @UiField SpanElement eventName;
    @UiField SpanElement venueName;
    @UiField SpanElement eventStartDate;
    @UiField Anchor linkToEventOverview;
    @UiField ImageElement eventImage;

    private EventDTO event;

    private final String defaultImageUrl = "http://static.sapsailing.com/newhome/event-1.jpg";

    interface RecentEventUiBinder extends UiBinder<Widget, RecentEvent> {
    }
    
    private static RecentEventUiBinder uiBinder = GWT.create(RecentEventUiBinder.class);

    public RecentEvent() {
        RecentEventResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void setEvent(EventDTO event) {
        this.event = event;
        updateUI();
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
