package com.sap.sailing.gwt.home.mobile.partials.recents;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.home.client.shared.LabelTypeUtil;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListEventSeriesDTO;
import com.sap.sailing.gwt.ui.shared.general.EventMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.LabelType;

public class EventsOverviewRecentYearEvent extends Widget {

    private static RecentEventTeaserUiBinder uiBinder = GWT.create(RecentEventTeaserUiBinder.class);

    interface RecentEventTeaserUiBinder extends UiBinder<Element, EventsOverviewRecentYearEvent> {
    }
    
    @UiField
    ImageElement eventImageUi;
    @UiField
    AnchorElement eventLinkUi;
    @UiField
    DivElement eventStateUi;
    @UiField
    SpanElement venueUi;
    @UiField
    SpanElement eventDateUi;
    @UiField
    SpanElement eventNameUi;
    
    private final Element eventContainerUi;

    public EventsOverviewRecentYearEvent(final PlaceNavigation<?> placeNavigation, final EventMetadataDTO event,
            LabelType labelType, boolean isTeaserEvent) {
        EventsOverviewRecentResources.INSTANCE.css().ensureInjected();
        setElement(eventContainerUi = uiBinder.createAndBindUi(this));
        eventNameUi.setInnerText(event.getDisplayName());
        if (isTeaserEvent) {
            eventImageUi.getStyle().setDisplay(Display.BLOCK);
            if(event.getThumbnailImageURL() == null || event.getThumbnailImageURL().isEmpty()) {
                eventImageUi.setSrc(EventsOverviewRecentResources.INSTANCE.defaultEventPhotoImage().getSafeUri().asString());
                
            }else {
                eventImageUi.setSrc(event.getThumbnailImageURL());
            }
        } else {
            eventImageUi.getStyle().setDisplay(Display.NONE);
        }
        eventLinkUi.setTitle(event.getDisplayName());
        eventLinkUi.setHref(placeNavigation.getTargetUrl());
        placeNavigation.configureAnchorElement(eventLinkUi);
        LabelTypeUtil.renderLabelType(eventStateUi, event.getState().getListStateMarker());
        venueUi.setInnerText(event.getLocationOrVenue());
        eventDateUi.setInnerText(EventDatesFormatterUtil.formatDateRangeWithoutYear(event.getStartDate(),
                event.getEndDate()));
    }

    public void setSeriesInformation(PlaceNavigation<?> seriesNavigation, EventListEventSeriesDTO eventSeries) {
        eventContainerUi.appendChild(new EventOverviewRecentSeriesInfo(seriesNavigation, eventSeries).getElement());
    }

}
