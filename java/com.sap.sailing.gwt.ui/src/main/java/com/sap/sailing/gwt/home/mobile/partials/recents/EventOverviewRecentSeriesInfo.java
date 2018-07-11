package com.sap.sailing.gwt.home.mobile.partials.recents;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.gwt.home.communication.eventlist.EventListEventSeriesDTO;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class EventOverviewRecentSeriesInfo extends UIObject {

    private static EventOverviewRecentSeriesInfoUiBinder uiBinder = GWT
            .create(EventOverviewRecentSeriesInfoUiBinder.class);

    interface EventOverviewRecentSeriesInfoUiBinder extends UiBinder<AnchorElement, EventOverviewRecentSeriesInfo> {
    }

    @UiField DivElement seriesNameUi;
    @UiField DivElement seriesInfoUi;
    private final AnchorElement seriesUi;
    
    public EventOverviewRecentSeriesInfo(PlaceNavigation<?> seriesNavigation, EventListEventSeriesDTO eventSeries) {
        setElement(seriesUi = uiBinder.createAndBindUi(this));
        seriesNameUi.setInnerText(eventSeries.getSeriesDisplayName());
        seriesInfoUi.setInnerText(StringMessages.INSTANCE.eventsHaveTakenPlace(eventSeries.getEventsCount()));
        seriesNavigation.configureAnchorElement(seriesUi);
    }

}
