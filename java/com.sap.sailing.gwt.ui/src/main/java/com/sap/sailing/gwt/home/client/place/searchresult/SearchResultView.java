package com.sap.sailing.gwt.home.client.place.searchresult;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.LeaderboardSearchResultDTO;

public interface SearchResultView {
    Widget asWidget();
    
    void updateSearchResult(String searchText, Iterable<LeaderboardSearchResultDTO> searchResultItems);
}
