package com.sap.sailing.gwt.home.shared.places.searchresult;

import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;

public interface SearchResultClientFactory extends ErrorAndBusyClientFactory, ClientFactoryWithDispatch {
    SearchResultView createSearchResultView();
}
