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

    interface SearchResultItemUiBinder extends UiBinder<DivElement, SearchResultItem> {
    }
    
    @UiField DivElement resultTitleUi;
    @UiField DivElement eventInfoContainerUi;
    @UiField AnchorElement anchorUi;
    private final MobilePlacesNavigator navigator;
    private final SearchResultDTO item;

    SearchResultItem(MobilePlacesNavigator navigator, SearchResultDTO item) {
        this.navigator = navigator;
        this.item = item;
        init(uiBinder.createAndBindUi(this), item);
    }

    @Override
    protected Element getResultTitleUi() {
        return resultTitleUi;
    }
    
    @Override
    protected void configureRegattaNavigation(String eventId, String leaderboardName, String baseUrl, boolean isOnRemoteServer) {
        navigator.getRegattaNavigation(eventId, leaderboardName, baseUrl, item.isOnRemoteServer()).configureAnchorElement(anchorUi);
    }
    
    @Override
    protected void addEventInfo(SearchResultEventInfoDTO event) {
        String eventId = String.valueOf(event.getId());
        PlaceNavigation<?> eventNavigation = navigator.getEventNavigation(eventId, item.getBaseUrl(), item.isOnRemoteServer());
        eventInfoContainerUi.appendChild(new SearchResultItemEventInfo(event, eventNavigation).getElement());
    }

}
