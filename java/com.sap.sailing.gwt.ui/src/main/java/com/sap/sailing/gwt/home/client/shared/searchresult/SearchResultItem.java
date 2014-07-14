package com.sap.sailing.gwt.home.client.shared.searchresult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventPlace.Tokenizer;
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
    @UiField Anchor eventOverviewLink;
    
    private final PlaceNavigator navigator;
    private LeaderboardSearchResultDTO searchResult;

    public SearchResultItem(PlaceNavigator navigator, LeaderboardSearchResultDTO searchResult) {
        this.navigator = navigator;
        this.searchResult = searchResult;
        
        SearchResultResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    
        regattaLink.setText(searchResult.getRegattaName());
        resultRegattaDetails.setInnerText("I have no idea yet what to show here...");
        eventOverviewLink.setText(searchResult.getEvent().getName());
        if(searchResult.getEvent().startDate != null) {
            resultEventDate.setInnerText(EventDatesFormatterUtil.formatDateRangeWithYear(searchResult.getEvent().startDate, searchResult.getEvent().endDate));
        } else {
            resultEventDate.setInnerText("Unknown date");
        }
    }

    @UiHandler("regattaLink")
    public void goToRegatta(ClickEvent e) {
        Window.alert("Not implemented yet.");
    }

    @UiHandler("eventOverviewLink")
    public void goToEventPlace(ClickEvent e) {
        EventBaseDTO event = searchResult.getEvent();
        
        if(searchResult.getBaseURL().contains("localhost") || searchResult.getBaseURL().contains("127.0.0.1")) {
            navigator.goToEvent(event.id.toString());
        } else {
            EventPlace eventPlace = new EventPlace(event.id.toString());
            EventPlace.Tokenizer t = new Tokenizer();
            String remoteEventUrl = searchResult.getBaseURL() + "/gwt/Home.html#" + EventPlace.class.getSimpleName() + ":" + t.getToken(eventPlace);
            Window.Location.replace(remoteEventUrl);
        }
    }
    
}
