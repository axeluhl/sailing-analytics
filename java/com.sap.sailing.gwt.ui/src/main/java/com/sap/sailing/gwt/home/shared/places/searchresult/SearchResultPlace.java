package com.sap.sailing.gwt.home.shared.places.searchresult;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.shared.app.HasLocationTitle;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class SearchResultPlace extends Place implements HasLocationTitle, HasMobileVersion {
    private final String searchText;
    
    public SearchResultPlace(String searchText) {
        super();
        this.searchText = searchText;
    }
    
    @Override
    public String getLocationTitle() {
        return StringMessages.INSTANCE.search();
    }

    public String getTitle() {
        return StringMessages.INSTANCE.sapSailing() + " - " + StringMessages.INSTANCE.search();
    }
    
    public String getSearchText() {
        return searchText;
    }

    public static class Tokenizer implements PlaceTokenizer<SearchResultPlace> {
        @Override
        public String getToken(SearchResultPlace place) {
            return place.getSearchText();
        }

        @Override
        public SearchResultPlace getPlace(String searchQuery) {
            return new SearchResultPlace(searchQuery);
        }
    }
}
