package com.sap.sailing.gwt.home.desktop.partials.searchresult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.sap.sailing.gwt.home.communication.search.SearchResultDTO;
import com.sap.sailing.gwt.home.communication.search.SearchResultDTO.EventInfoDTO;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.partials.searchresult.AbstractSearchResultItem;

public class SearchResultItem extends AbstractSearchResultItem {
    
    private static SearchResultUiBinder uiBinder = GWT.create(SearchResultUiBinder.class);

    interface SearchResultUiBinder extends UiBinder<Element, SearchResultItem> {
    }
    
    @UiField AnchorElement resultTitleUi;
    @UiField SpanElement resultDescriptionUi;
    @UiField AnchorElement eventNameUi;
    @UiField SpanElement eventVenueUi;
    @UiField SpanElement eventDateUi;
    
    SearchResultItem(DesktopPlacesNavigator navigator, SearchResultDTO item) {
        init(uiBinder.createAndBindUi(this), item);
        EventInfoDTO event = item.getEvents().get(0);
        String eventId = String.valueOf(event.getId()), leaderboardName = item.getLeaderboardName(), baseUrl = item.getBaseUrl();
        PlaceNavigation<?> regattaNavigation = navigator.getRegattaNavigation(eventId, leaderboardName, baseUrl, item.isOnRemoteServer());
        regattaNavigation.configureAnchorElement(resultTitleUi);
        PlaceNavigation<?> eventNavigation = navigator.getEventNavigation(eventId, baseUrl, item.isOnRemoteServer());
        eventNavigation.configureAnchorElement(eventNameUi);
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
