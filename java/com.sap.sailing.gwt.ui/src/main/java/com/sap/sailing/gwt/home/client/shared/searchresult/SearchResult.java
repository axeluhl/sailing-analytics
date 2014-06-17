package com.sap.sailing.gwt.home.client.shared.searchresult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;

public class SearchResult extends Composite {
    @SuppressWarnings("unused")
    private final PlaceNavigator navigator;

    interface HeaderUiBinder extends UiBinder<Widget, SearchResult> {
    }
    
    private static HeaderUiBinder uiBinder = GWT.create(HeaderUiBinder.class);

    public SearchResult(PlaceNavigator navigator) {
        this.navigator = navigator;
        SearchResultResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

}
