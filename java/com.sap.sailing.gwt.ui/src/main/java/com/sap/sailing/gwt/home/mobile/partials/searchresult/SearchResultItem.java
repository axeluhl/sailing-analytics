package com.sap.sailing.gwt.home.mobile.partials.searchresult;

import static com.sap.sailing.gwt.home.shared.utils.EventDatesFormatterUtil.formatDateRangeWithYear;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.shared.dispatch.search.SearchResultDTO;

public class SearchResultItem extends Widget {

    private static SearchResultItemUiBinder uiBinder = GWT.create(SearchResultItemUiBinder.class);

    interface SearchResultItemUiBinder extends UiBinder<Element, SearchResultItem> {
    }
    
    @UiField AnchorElement resultLinkUi;
    @UiField AnchorElement eventLinkUi;
    @UiField SpanElement eventVenueUi;
    @UiField SpanElement eventDateUi;

    public SearchResultItem(MobilePlacesNavigator navigator, SearchResultDTO result) {
        setElement(uiBinder.createAndBindUi(this));
        resultLinkUi.setInnerText(result.getDisplayName());
        String eventId = result.getEventId().toString(), baseUrl = result.getBaseUrl();
        PlaceNavigation<?> regattaNavigation = navigator.getRegattaOverviewNavigation(eventId, result.getLeaderboardName(), baseUrl, result.isOnRemoteServer());
        regattaNavigation.configureAnchorElement(resultLinkUi);
        eventLinkUi.setInnerText(result.getEventName());
        PlaceNavigation<?> eventNavigation = navigator.getEventNavigation(eventId, baseUrl, result.isOnRemoteServer());
        eventNavigation.configureAnchorElement(eventLinkUi);
        eventVenueUi.setInnerText(result.getEventVenueName());
        if (result.getEventStartDate() != null) {
            eventDateUi.setInnerText(formatDateRangeWithYear(result.getEventStartDate(), result.getEventEndDate()));
        }
    }

}
