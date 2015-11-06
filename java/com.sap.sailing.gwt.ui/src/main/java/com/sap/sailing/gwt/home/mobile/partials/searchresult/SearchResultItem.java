package com.sap.sailing.gwt.home.mobile.partials.searchresult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.sap.sailing.gwt.home.communication.search.SearchResultDTO;
import com.sap.sailing.gwt.home.communication.search.SearchResultDTO.EventInfoDTO;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.partials.searchresult.AbstractSearchResultItem;

public class SearchResultItem extends AbstractSearchResultItem {

    private static SearchResultItemUiBinder uiBinder = GWT.create(SearchResultItemUiBinder.class);

    interface SearchResultItemUiBinder extends UiBinder<AnchorElement, SearchResultItem> {
    }
    
    @UiField DivElement resultTitleUi;
    @UiField DivElement eventNameUi;
    @UiField SpanElement eventVenueUi;
    @UiField SpanElement eventDateUi;
    private final AnchorElement anchorUi;

    SearchResultItem(MobilePlacesNavigator navigator, SearchResultDTO item) {
        init(anchorUi = uiBinder.createAndBindUi(this), item);
        EventInfoDTO event = item.getEvents().get(0);
        String eventId = String.valueOf(event.getId()), leaderboardName = item.getLeaderboardName(), baseUrl = item.getBaseUrl();
        PlaceNavigation<?> regattaNavigation = navigator.getRegattaOverviewNavigation(eventId, leaderboardName, baseUrl, item.isOnRemoteServer());
        regattaNavigation.configureAnchorElement(anchorUi);
    }

    @Override
    protected Element getResultTitleUi() {
        return resultTitleUi;
    }

    @Override
    protected Element getEventNameUi() {
        return eventNameUi;
    }

    @Override
    protected Element getEventVenueUi() {
        return eventVenueUi;
    }

    @Override
    protected Element getEventDateUi() {
        return eventDateUi;
    }

}
