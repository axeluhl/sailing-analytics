package com.sap.sailing.gwt.home.client.place.searchresult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.shared.searchresult.SearchResult;

public class TabletAndDesktopSearchResultView extends Composite implements SearchResultView {
    private static SearchResultUiBinder uiBinder = GWT.create(SearchResultUiBinder.class);

    interface SearchResultUiBinder extends UiBinder<Widget, TabletAndDesktopSearchResultView> {
    }

    @UiField(provided=true) SearchResult searchResult;
    
    public TabletAndDesktopSearchResultView(PlaceNavigator navigator) {
        searchResult = new SearchResult(navigator);
        initWidget(uiBinder.createAndBindUi(this));
    }
}
