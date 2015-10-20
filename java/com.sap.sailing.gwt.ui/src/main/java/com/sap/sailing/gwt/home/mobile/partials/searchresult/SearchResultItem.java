package com.sap.sailing.gwt.home.mobile.partials.searchresult;

import static com.sap.sailing.gwt.home.shared.utils.EventDatesFormatterUtil.formatDateRangeWithYear;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.shared.dispatch.search.SearchResultDTO;

public class SearchResultItem extends Widget {

    private static SearchResultItemUiBinder uiBinder = GWT.create(SearchResultItemUiBinder.class);

    interface SearchResultItemUiBinder extends UiBinder<AnchorElement, SearchResultItem> {
    }
    
    @UiField DivElement resultTitleUi;
    @UiField DivElement eventNameUi;
    @UiField SpanElement eventVenueUi;
    @UiField SpanElement eventDateUi;
    private final AnchorElement anchorUi;

    public SearchResultItem(MobilePlacesNavigator navigator, SearchResultDTO result) {
        setElement(anchorUi = uiBinder.createAndBindUi(this));
        resultTitleUi.setInnerText(result.getDisplayName());
        String eventId = String.valueOf(result.getEventId()), leaderboardName = result.getLeaderboardName(), baseUrl = result.getBaseUrl();
        PlaceNavigation<?> regattaNavigation = navigator.getRegattaOverviewNavigation(eventId, leaderboardName, baseUrl, result.isOnRemoteServer());
        regattaNavigation.configureAnchorElement(anchorUi);
        eventNameUi.setInnerText(result.getEventName());
        eventVenueUi.setInnerText(result.getEventVenueName());
        if (result.getEventStartDate() != null && result.getEventEndDate() != null) {
            eventDateUi.setInnerText(formatDateRangeWithYear(result.getEventStartDate(), result.getEventEndDate()));
        }
    }

}
