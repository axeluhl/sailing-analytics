package com.sap.sailing.gwt.home.client.shared.recentevent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event2.EventDefaultPlace;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.home.client.shared.LongNamesUtil;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class RecentEvent extends Composite {

    @UiField
    SpanElement eventName;
    @UiField
    SpanElement venueName;
    @UiField
    SpanElement eventStartDate;
    @UiField
    Anchor eventOverviewLink;
    @UiField
    DivElement eventImageContainerDiv;
    @UiField
    DivElement isLiveDiv;

    private final EventBaseDTO event;

    private final HomePlacesNavigator navigator;
    private final PlaceNavigation<EventDefaultPlace> eventNavigation;

    interface RecentEventUiBinder extends UiBinder<Widget, RecentEvent> {
    }

    private static RecentEventUiBinder uiBinder = GWT.create(RecentEventUiBinder.class);

    public RecentEvent(final HomePlacesNavigator navigator, final EventBaseDTO event) {
        this.navigator = navigator;
        this.event = event;

        RecentEventResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));

        eventNavigation = navigator.getEventNavigation(event.id.toString(), event.getBaseURL(),
                event.isOnRemoteServer());
        eventOverviewLink.setHref(eventNavigation.getTargetUrl());

        updateUI();
    }

    private void updateUI() {
        SafeHtml safeHtmlEventName = LongNamesUtil.breakLongName(event.getName());
        eventName.setInnerSafeHtml(safeHtmlEventName);
        if (!event.isRunning()) {
            isLiveDiv.getStyle().setDisplay(Display.NONE);
        }
        venueName.setInnerText(event.venue.getName());
        eventStartDate.setInnerText(EventDatesFormatterUtil.formatDateRangeWithoutYear(event.startDate, event.endDate));

        final StringBuilder thumbnailUrlBuilder = new StringBuilder("url('");

        final String thumbnailImageUrl = event.getEventThumbnailImageUrl();
        if (thumbnailImageUrl == null || thumbnailImageUrl.isEmpty()) {
            thumbnailUrlBuilder.append(RecentEventResources.INSTANCE.defaultEventPhotoImage().getSafeUri().asString());
        } else {
            thumbnailUrlBuilder.append(UriUtils.fromString(thumbnailImageUrl).asString());
        }
        thumbnailUrlBuilder.append("')");
        eventImageContainerDiv.getStyle().setBackgroundImage(thumbnailUrlBuilder.toString());
    }

    @UiHandler("eventOverviewLink")
    public void goToEventOverview(ClickEvent e) {
        navigator.goToPlace(eventNavigation);
        e.preventDefault();
    }

}
