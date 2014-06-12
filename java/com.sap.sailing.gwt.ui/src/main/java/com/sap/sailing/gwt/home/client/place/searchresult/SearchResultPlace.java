package com.sap.sailing.gwt.home.client.place.searchresult;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class SearchResultPlace extends Place {
    private final String searchQuery;
    
    public SearchResultPlace(String searchQuery) {
        super();
        this.searchQuery = searchQuery;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public static class Tokenizer implements PlaceTokenizer<SearchResultPlace> {
        @Override
        public String getToken(SearchResultPlace place) {
            return place.getSearchQuery();
        }

        @Override
        public SearchResultPlace getPlace(String searchQuery) {
            return new SearchResultPlace(searchQuery);
        }
    }
}
