package com.sap.sailing.gwt.home.desktop.places.searchresult;

import com.google.gwt.event.shared.GwtEvent;

public class SearchEvent extends GwtEvent<SearchEventHandler> {
    public static Type<SearchEventHandler> TYPE = new Type<SearchEventHandler>();

    private final String searchText;

    public SearchEvent(String searchText) {
        this.searchText = searchText;
    }

    public String getSearchText() {
        return searchText;
    }

    @Override
    public Type<SearchEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SearchEventHandler handler) {
        handler.onDoSearch(this);
    }
}
