package com.sap.sailing.gwt.regattaoverview.client.eventheader;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;

public class EventHeader extends Composite {
    private static EventHeaderUiBinder uiBinder = GWT.create(EventHeaderUiBinder.class);

    interface EventHeaderUiBinder extends UiBinder<Widget, EventHeader> {
    }
    
    
    @UiField DivElement eventLogo;
    @UiField SpanElement eventName;
    @UiField DivElement eventDate;
    @UiField SpanElement eventVenue;
    @UiField DivElement eventVenueContainer;


    
    public EventHeader() {
        EventHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setLogoUrl(String displayName, Date startDate, Date endDate, String venue, String url) {
        final String logoUrl = url != null ? url : EventHeaderResources.INSTANCE.defaultEventLogoImage().getSafeUri()
                .asString();
        eventLogo.getStyle().setBackgroundImage("url(" + logoUrl + ")");
        eventLogo.setTitle(displayName);
        eventDate.setInnerHTML(EventDatesFormatterUtil.formatDateRangeWithYear(startDate, endDate));
        eventVenue.setInnerText(venue);
        // String venue = event.getLocationAndVenue();
        // if(event.getVenueCountry() != null && !event.getVenueCountry().isEmpty()) {
        // venue += ", " + event.getVenueCountry();
        // }
    }



}
