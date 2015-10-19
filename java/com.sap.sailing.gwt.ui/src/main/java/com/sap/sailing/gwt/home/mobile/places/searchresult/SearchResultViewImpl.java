package com.sap.sailing.gwt.home.mobile.places.searchresult;

import java.util.Collection;

import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.mobile.partials.searchresult.SearchResult;
import com.sap.sailing.gwt.ui.shared.dispatch.search.SearchResultDTO;

public class SearchResultViewImpl extends Composite implements SearchResultView {
    
    private final SearchResult searchResultUi;

    public SearchResultViewImpl(Presenter presenter) {
        initWidget(searchResultUi = new SearchResult(presenter));
    }

    @Override
    public void updateSearchResult(String searchText, Collection<SearchResultDTO> searchResultItems) {
        searchResultUi.updateSearchResult(searchText, searchResultItems);
    }

}
