package com.sap.sailing.gwt.home.mobile.places.searchresult;

import java.util.Collection;

import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.communication.search.SearchResultDTO;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.mobile.partials.searchresult.SearchResult;
import com.sap.sailing.gwt.home.shared.places.searchresult.SearchResultView;

public class SearchResultViewImpl extends Composite implements SearchResultView {
    
    private final SearchResult searchResultUi;

    public SearchResultViewImpl(MobilePlacesNavigator navigator) {
        initWidget(searchResultUi = new SearchResult(navigator));
    }

    @Override
    public void updateSearchResult(String searchText, Collection<SearchResultDTO> searchResultItems) {
        searchResultUi.updateSearchResult(searchText, searchResultItems);
    }

}
