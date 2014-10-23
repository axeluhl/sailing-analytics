package com.sap.sailing.gwt.home.client.shared.searchresult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardSearchResultDTO;

public class SearchResultItem extends Composite {
    interface SearchResultUiBinder extends UiBinder<Widget, SearchResultItem> {
    }
    
    private static SearchResultUiBinder uiBinder = GWT.create(SearchResultUiBinder.class);

    @UiField Anchor regattaLink;
    @UiField SpanElement resultRegattaDetails;
    @UiField SpanElement resultEventDate;
    @UiField SpanElement resultEventVenue;
    @UiField Anchor eventOverviewLink;
    
    private final PlaceNavigator placeNavigator;
    private LeaderboardSearchResultDTO searchResult;

    public SearchResultItem(PlaceNavigator navigator, LeaderboardSearchResultDTO searchResult) {
        this.placeNavigator = navigator;
        this.searchResult = searchResult;
        
        SearchResultResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    
        String headlineLink = searchResult.getLeaderboardDisplayName();
        if(headlineLink == null) {
            headlineLink = searchResult.getRegattaName() != null ? searchResult.getRegattaName() : searchResult.getLeaderboardName();
        }
        
        regattaLink.setText(headlineLink);
//        resultRegattaDetails.setInnerText("I have no idea yet what to show here...");
        eventOverviewLink.setText(searchResult.getEvent().getName());
        resultEventVenue.setInnerText(searchResult.getEvent().venue.getName());
        if(searchResult.getEvent().startDate != null) {
            resultEventDate.setInnerText(EventDatesFormatterUtil.formatDateRangeWithYear(searchResult.getEvent().startDate, searchResult.getEvent().endDate));
        } else {
            resultEventDate.setInnerText("Unknown date");
        }
    }

    @UiHandler("regattaLink")
    public void goToRegatta(ClickEvent e) {
        EventBaseDTO event = searchResult.getEvent();
        PlaceNavigation<EventPlace> regattaNavigation = placeNavigator.getRegattaNavigation(event.id.toString(), 
                searchResult.getLeaderboardName(), searchResult.getBaseURL(), searchResult.isOnRemoteServer());
        placeNavigator.goToPlace(regattaNavigation);
    }

    @UiHandler("eventOverviewLink")
    public void goToEventPlace(ClickEvent e) {
        EventBaseDTO event = searchResult.getEvent();
        placeNavigator.getEventNavigation(event.id.toString(), searchResult.getBaseURL(), searchResult.isOnRemoteServer());
    }
}
