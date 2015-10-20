package com.sap.sailing.gwt.home.mobile.partials.recents;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.utils.EventDatesFormatterUtil;
import com.sap.sailing.gwt.home.shared.utils.LabelTypeUtil;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListEventSeriesDTO;
import com.sap.sailing.gwt.ui.shared.general.EventMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.LabelType;

public class EventsOverviewRecentYearEvent extends Widget {

    private static RecentEventTeaserUiBinder uiBinder = GWT.create(RecentEventTeaserUiBinder.class);

    interface RecentEventTeaserUiBinder extends UiBinder<Element, EventsOverviewRecentYearEvent> {
    }
    
    private final Element eventContainerUi;

    @UiField AnchorElement eventLinkUi;
    @UiField DivElement eventStateUi;
    @UiField SpanElement venueUi;
    @UiField SpanElement eventDateUi;
    @UiField SpanElement eventNameUi;

    public EventsOverviewRecentYearEvent(final PlaceNavigation<?> placeNavigation, final EventMetadataDTO event,
            LabelType labelType, boolean isTeaserEvent) {
        EventsOverviewRecentResources.INSTANCE.css().ensureInjected();
        setElement(eventContainerUi = uiBinder.createAndBindUi(this));
        eventNameUi.setInnerText(event.getDisplayName());
        eventLinkUi.setTitle(event.getDisplayName());
        eventLinkUi.setHref(placeNavigation.getTargetUrl());
        placeNavigation.configureAnchorElement(eventLinkUi);
        LabelTypeUtil.renderLabelType(eventStateUi, event.getState().getListStateMarker());
        venueUi.setInnerText(event.getLocationOrVenue());
        eventDateUi.setInnerText(EventDatesFormatterUtil.formatDateRangeWithoutYear(event.getStartDate(), event.getEndDate()));
    }

    public void setSeriesInformation(PlaceNavigation<?> seriesNavigation, EventListEventSeriesDTO eventSeries) {
        eventContainerUi.appendChild(new EventOverviewRecentSeriesInfo(seriesNavigation, eventSeries).getElement());
    }
}
