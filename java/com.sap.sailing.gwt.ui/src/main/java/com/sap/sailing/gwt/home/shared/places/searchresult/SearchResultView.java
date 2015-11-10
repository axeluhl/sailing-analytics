package com.sap.sailing.gwt.home.shared.places.searchresult;

import java.util.Collection;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.communication.search.SearchResultDTO;

public interface SearchResultView extends IsWidget {
    
    void setSearchText(String searchText);
    
    void updateSearchResult(String searchText, Collection<SearchResultDTO> searchResultItems);
    
    public interface Presenter {
       
    }

}
