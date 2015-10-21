package com.sap.sailing.gwt.home.desktop.places.searchresult;

import com.sap.sailing.gwt.home.shared.places.searchresult.SearchResultView;
import com.sap.sailing.gwt.ui.client.refresh.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;

public interface SearchResultClientFactory extends ErrorAndBusyClientFactory, ClientFactoryWithDispatch {
    SearchResultView createSearchResultView();
}
