package com.sap.sailing.gwt.home.client.place.event.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventHeader extends Composite {
    private static EventHeaderUiBinder uiBinder = GWT.create(EventHeaderUiBinder.class);

    interface EventHeaderUiBinder extends UiBinder<Widget, EventHeader> {
    }

    private final EventDTO event;
    
    @UiField SpanElement eventName;
    @UiField SpanElement eventName2;
    @UiField SpanElement eventName3;
    @UiField SpanElement eventDate;
    @UiField SpanElement eventDescription;
    @UiField SpanElement venueName;
    @UiField SpanElement eventWebsite;
    
    @UiField ImageElement eventLogo;
    @UiField ImageElement eventLogo2;
    @UiField ImageElement eventLogo3;

    private final String defaultLogoUrl = "http://static.sapsailing.com/newhome/default_event_logo.png";

    public EventHeader(EventDTO event) {
        this.event = event;
        
        EventHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        updateUI();
    }
    
    private void updateUI() {
        eventName.setInnerHTML(event.getName());
        eventName2.setInnerHTML(event.getName()+"2");
        eventName3.setInnerHTML(event.getName()+"3");
        venueName.setInnerHTML(event.venue.getName());
        eventDate.setInnerHTML(EventDatesFormatterUtil.formatDateRangeWithYear(event.startDate, event.endDate));
        
        eventDescription.setInnerHTML("Missing attribute: The description of the event");
        eventWebsite.setInnerHTML("Missing link to official event website");
        eventLogo.setSrc(defaultLogoUrl);
        eventLogo2.setSrc(defaultLogoUrl);
        eventLogo3.setSrc(defaultLogoUrl);
    }
    
}
