package com.sap.sailing.gwt.home.client.shared.searchresult;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultPlace;
import com.sap.sailing.gwt.ui.shared.LeaderboardSearchResultDTO;

public class SearchResult extends Composite {
    interface SearchResultUiBinder extends UiBinder<Widget, SearchResult> {
    }
    
    private static SearchResultUiBinder uiBinder = GWT.create(SearchResultUiBinder.class);

    @UiField SpanElement searchResultFor;
    @UiField SpanElement searchResultCount;
    
    @UiField TextBox searchText1;
    @UiField Button searchButton1;
    @UiField TextBox searchText2;
    @UiField Button searchButton2;
    @UiField HTMLPanel searchResultItemPanel;

    private final List<SearchResultItem> searchResultItemComposites;
    private final HomePlacesNavigator navigator;

    private int resultCounter;

    public SearchResult(HomePlacesNavigator navigator) {
        this.navigator = navigator;
        this.searchResultItemComposites = new ArrayList<SearchResultItem>();
        
        SearchResultResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        searchText1.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    searchButton1.click();
                }
            }
        });
        searchText2.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    searchButton2.click();
                }
            }
        });
    }

    @UiHandler("searchButton1")
    void searchButton1Click(ClickEvent event) {
        String searchText = searchText1.getText();
        doSearch(searchText);
    }

    @UiHandler("searchButton2")
    void searchButton2Click(ClickEvent event) {
        String searchText = searchText2.getText();
        doSearch(searchText);
    }

    private void doSearch(String searchText) {
        if(searchText.isEmpty()) {
            Window.alert("Please enter a search term.");
        } else {
            PlaceNavigation<SearchResultPlace> searchResultNavigation = navigator.getSearchResultNavigation(searchText);
            navigator.goToPlace(searchResultNavigation);
        }
    }

    public void updateSearchResult(String searchText, Iterable<LeaderboardSearchResultDTO> searchResultItems) {
        for (LeaderboardSearchResultDTO singleSearchResult : searchResultItems) {
            // for now filter all results where we no event is defined
            if (singleSearchResult.getEvent() != null) {
                SearchResultItem searchResultItem = new SearchResultItem(navigator, singleSearchResult);
                searchResultItemPanel.add(searchResultItem);
                searchResultItemComposites.add(searchResultItem);
                resultCounter++;
            }
        }
        searchResultCount.setInnerText(String.valueOf(resultCounter));
    }

    public void init(String searchText) {
        searchResultItemPanel.clear();
        searchResultItemComposites.clear();
        
        searchResultFor.setInnerText(searchText);
        searchText1.setText(searchText);
        searchText2.setText(searchText);
        
        resultCounter = 0;
    }
}
