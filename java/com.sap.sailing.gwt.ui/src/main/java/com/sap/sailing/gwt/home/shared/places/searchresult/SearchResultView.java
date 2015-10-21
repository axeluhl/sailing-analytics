package com.sap.sailing.gwt.home.shared.places.searchresult;

import java.util.Collection;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.shared.app.AbstractPlaceNavigator;
import com.sap.sailing.gwt.ui.shared.dispatch.search.SearchResultDTO;

public interface SearchResultView extends IsWidget {
    
    void updateSearchResult(String searchText, Collection<SearchResultDTO> searchResultItems);
    
    public interface Presenter {
       
        AbstractPlaceNavigator getNavigator();
        
    }

}
