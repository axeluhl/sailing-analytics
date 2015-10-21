package com.sap.sailing.gwt.home.mobile.places.searchresult;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.shared.places.searchresult.SearchResultPlace;
import com.sap.sailing.gwt.home.shared.places.searchresult.SearchResultView;
import com.sap.sailing.gwt.ui.shared.dispatch.ListResult;
import com.sap.sailing.gwt.ui.shared.dispatch.search.GetSearchResultsAction;
import com.sap.sailing.gwt.ui.shared.dispatch.search.SearchResultDTO;

public class SearchResultActivity extends AbstractActivity implements SearchResultView.Presenter {

    private final MobileApplicationClientFactory clientFactory;
    private final SearchResultPlace searchResultPlace;
    
    public SearchResultActivity(SearchResultPlace place, MobileApplicationClientFactory clientFactory) {
        this.searchResultPlace = place;
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        final SearchResultView view = new SearchResultViewImpl(clientFactory.getNavigator());
        panel.setWidget(view);
        if (searchResultPlace.getSearchText() != null && !searchResultPlace.getSearchText().isEmpty()) {
            final String searchText = searchResultPlace.getSearchText();
            clientFactory.getDispatch().execute(new GetSearchResultsAction(searchText), 
                    new AsyncCallback<ListResult<SearchResultDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    panel.setWidget(clientFactory.createErrorView("Error while executing search!", caught));
                }
                
                @Override
                public void onSuccess(ListResult<SearchResultDTO> result) {
                    view.updateSearchResult(searchText, result.getValues());
                }
            });
        }
    }
    
    @Override
    public MobilePlacesNavigator getNavigator() {
        return clientFactory.getNavigator();
    }

}
