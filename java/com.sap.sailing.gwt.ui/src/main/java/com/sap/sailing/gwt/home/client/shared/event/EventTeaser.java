package com.sap.sailing.gwt.home.client.shared.event;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.home.client.shared.LabelTypeUtil;
import com.sap.sailing.gwt.home.client.shared.LongNamesUtil;
import com.sap.sailing.gwt.ui.shared.general.EventMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.LabelType;

public class EventTeaser extends Composite {

    @UiField
    DivElement eventTeaserWithState;
    @UiField
    DivElement eventNameWithState;
    @UiField
    DivElement eventName;
    @UiField
    SpanElement venue;
    @UiField
    SpanElement eventDate;
    @UiField
    AnchorElement eventLink;
    @UiField
    DivElement eventImage;
    @UiField
    DivElement eventState;

    private final EventMetadataDTO event;

    interface RecentEventUiBinder extends UiBinder<Widget, EventTeaser> {
    }

    private static RecentEventUiBinder uiBinder = GWT.create(RecentEventUiBinder.class);

    public EventTeaser(final PlaceNavigation<?> placeNavigation, final EventMetadataDTO event) {
        this.event = event;

        EventTeaserResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        eventLink.setHref(placeNavigation.getTargetUrl());
        
        Event.sinkEvents(eventLink, Event.ONCLICK);
        Event.setEventListener(eventLink, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if(event.getTypeInt() == Event.ONCLICK) {
                    event.preventDefault();
                    placeNavigation.goToPlace();
                }
            }
        });;

        updateUI();
    }

    private void updateUI() {
        SafeHtml safeHtmlEventName = LongNamesUtil.breakLongName(event.getDisplayName());
        LabelType labelType = event.getState().getStateMarker();
        if(labelType.getLabelType() == null) {
            eventTeaserWithState.removeFromParent();
            eventName.setInnerSafeHtml(safeHtmlEventName);
        } else {
            eventNameWithState.setInnerSafeHtml(safeHtmlEventName);
            LabelTypeUtil.renderLabelType(eventState, labelType);
        }
        
        String venueString = event.getVenue();
        if(event.getVenueCountry()!= null && ! event.getVenueCountry().isEmpty()) {
            venueString += ", " + event.getVenueCountry();
        }
        venue.setInnerText(venueString);
        eventDate.setInnerText(EventDatesFormatterUtil.formatDateRangeWithoutYear(event.getStartDate(), event.getEndDate()));

        final StringBuilder thumbnailUrlBuilder = new StringBuilder("url('");

        final String thumbnailImageUrl = event.getLogoImageURL();
        if (thumbnailImageUrl == null || thumbnailImageUrl.isEmpty()) {
            thumbnailUrlBuilder.append(EventTeaserResources.INSTANCE.defaultEventPhotoImage().getSafeUri().asString());
        } else {
            thumbnailUrlBuilder.append(UriUtils.fromString(thumbnailImageUrl).asString());
        }
        thumbnailUrlBuilder.append("')");
        eventImage.getStyle().setBackgroundImage(thumbnailUrlBuilder.toString());
    }

}
