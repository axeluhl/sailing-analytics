package com.sap.sailing.gwt.home.desktop.places.searchresult;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.desktop.partials.searchresult.SearchResult;
import com.sap.sailing.gwt.home.shared.places.searchresult.SearchResultView;
import com.sap.sailing.gwt.ui.shared.dispatch.search.SearchResultDTO;

public class TabletAndDesktopSearchResultView extends Composite implements SearchResultView {
    private static SearchResultUiBinder uiBinder = GWT.create(SearchResultUiBinder.class);

    interface SearchResultUiBinder extends UiBinder<Widget, TabletAndDesktopSearchResultView> {
    }

    @UiField(provided=true) SearchResult searchResult;
    
    public TabletAndDesktopSearchResultView(DesktopPlacesNavigator navigator) {
        searchResult = new SearchResult(navigator);
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public void updateSearchResult(String searchText, Collection<SearchResultDTO> searchResultItems) {
        searchResult.updateSearchResult(searchText, searchResultItems);
    }

}
