package com.sap.sailing.gwt.home.desktop.places.searchresult;

import com.sap.sailing.gwt.home.shared.places.searchresult.SearchResultView;
import com.sap.sailing.gwt.ui.client.SailingClientFactory;
import com.sap.sailing.gwt.ui.client.refresh.ClientFactoryWithDispatch;

public interface SearchResultClientFactory extends SailingClientFactory, ClientFactoryWithDispatch {
    SearchResultView createSearchResultView();
}
