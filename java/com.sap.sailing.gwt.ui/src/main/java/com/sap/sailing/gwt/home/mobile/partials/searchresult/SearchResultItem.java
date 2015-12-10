package com.sap.sailing.gwt.home.mobile.partials.searchresult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.sap.sailing.gwt.home.communication.search.SearchResultDTO;
import com.sap.sailing.gwt.home.communication.search.SearchResultEventInfoDTO;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.partials.searchresult.AbstractSearchResultItem;

public class SearchResultItem extends AbstractSearchResultItem {

    private static SearchResultItemUiBinder uiBinder = GWT.create(SearchResultItemUiBinder.class);

    interface SearchResultItemUiBinder extends UiBinder<AnchorElement, SearchResultItem> {
    }
    
    @UiField DivElement resultTitleUi;
    @UiField DivElement eventInfoContainerUi;
    private final AnchorElement anchorUi;
    private final MobilePlacesNavigator navigator;

    SearchResultItem(MobilePlacesNavigator navigator, SearchResultDTO item) {
        this.navigator = navigator;
        init(anchorUi = uiBinder.createAndBindUi(this), item);
        SearchResultEventInfoDTO event = item.getEvents().iterator().next();
        String eventId = String.valueOf(event.getId()), leaderboardName = item.getLeaderboardName(), baseUrl = item.getBaseUrl();
        PlaceNavigation<?> regattaNavigation = navigator.getRegattaOverviewNavigation(eventId, leaderboardName, baseUrl, item.isOnRemoteServer());
        regattaNavigation.configureAnchorElement(anchorUi);
    }

    @Override
    protected Element getResultTitleUi() {
        return resultTitleUi;
    }
    
    @Override
    protected void addEventInfo(SearchResultEventInfoDTO event) {
        if (eventInfoContainerUi.hasChildNodes()) return; // TODO: Temporary add only one event on mobile
        eventInfoContainerUi.appendChild(new SearchResultItemEventInfo(navigator, event).getElement());
    }

}
