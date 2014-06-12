package com.sap.sailing.gwt.home.client.place.events.upcoming;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class UpcomingEvent extends Composite {

    @UiField SpanElement eventName;
    @UiField SpanElement seriesName;
    @UiField SpanElement venueName;
    @UiField SpanElement eventStartDate;

    @SuppressWarnings("unused")
    private final PlaceNavigator navigator;

    private EventBaseDTO event;


    interface UpcomingEventUiBinder extends UiBinder<Widget, UpcomingEvent> {
    }
    
    private static UpcomingEventUiBinder uiBinder = GWT.create(UpcomingEventUiBinder.class);

    public UpcomingEvent(PlaceNavigator navigator) {
        this.navigator = navigator;

        EventsOverviewUpcomingResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void setEvent(EventBaseDTO event) {
        this.event = event;
        updateUI();
    }

    private void updateUI() {
        eventName.setInnerText(event.getName());
        venueName.setInnerText(event.venue.getName());
        eventStartDate.setInnerText(EventDatesFormatterUtil.formatDateRangeWithoutYear(event.startDate, event.endDate));
    }        
}
