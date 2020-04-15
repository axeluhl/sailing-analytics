package com.sap.sailing.gwt.home.desktop.partials.event;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.gwt.home.communication.eventlist.EventListEventSeriesDTO;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class EventTeaserSeriesInfoCorner extends UIObject {

    private static EventTeaserSeriesInfoCornerUiBinder uiBinder = GWT.create(EventTeaserSeriesInfoCornerUiBinder.class);

    interface EventTeaserSeriesInfoCornerUiBinder extends UiBinder<DivElement, EventTeaserSeriesInfoCorner> {
    }
    
    @UiField DivElement seriesEventsCountUi;
    @UiField AnchorElement seriesInfoTextUi;

    public EventTeaserSeriesInfoCorner(PlaceNavigation<?> seriesNavigation, EventListEventSeriesDTO eventSeries) {
        setElement(uiBinder.createAndBindUi(this));
        int eventCount = eventSeries.getEventsCount();
        seriesInfoTextUi.setTitle(eventSeries.getSeriesDisplayName() + " - " + 
                StringMessages.INSTANCE.eventsHaveTakenPlace(eventSeries.getEventsCount()));
        seriesNavigation.configureAnchorElement(seriesInfoTextUi);
        seriesEventsCountUi.setInnerText(String.valueOf(eventCount));
    }

}
