package com.sap.sailing.gwt.home.desktop.places.searchresult;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.ui.shared.LeaderboardSearchResultDTO;

public interface SearchResultView extends IsWidget {

    void initSearchResult(String searchText);
    
    void updateSearchResult(String searchText, Iterable<LeaderboardSearchResultDTO> searchResultItems);
}
