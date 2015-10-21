package com.sap.sailing.gwt.home.mobile.partials.searchresult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.shared.partials.searchresult.AbstractSearchResult;
import com.sap.sailing.gwt.ui.shared.dispatch.search.SearchResultDTO;

public class SearchResult extends AbstractSearchResult {

    private static SearchResultUiBinder uiBinder = GWT.create(SearchResultUiBinder.class);

    interface SearchResultUiBinder extends UiBinder<Widget, SearchResult> {
    }

    @UiField TextBox searchInputUi;
    @UiField Button searchButtonUi;
    @UiField DivElement searchResultAmountUi;
    @UiField FlowPanel searchResultContainerUi;
    
    private final MobilePlacesNavigator navigator;
    
    public SearchResult(MobilePlacesNavigator navigator) {
        this.navigator = navigator;
        SearchResultResources.INSTANCE.css().ensureInjected();
        init(navigator, uiBinder.createAndBindUi(this));
    }

    @Override
    protected TextBox getSearchTextInputUi() {
        return searchInputUi;
    }

    @Override
    protected Button getSearchButtonUi() {
        return searchButtonUi;
    }

    @Override
    protected Element getSearchResultAmountUi() {
        return searchResultAmountUi;
    }

    @Override
    protected HasWidgets getSearchResultContainerUi() {
        return searchResultContainerUi;
    }

    @Override
    protected void addSearchResultItem(SearchResultDTO searchResult) {
        searchResultContainerUi.add(new SearchResultItem(navigator, searchResult));
    }

}
