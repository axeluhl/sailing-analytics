package com.sap.sailing.gwt.home.shared.places.searchresult;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.dispatch.client.ListResult;
import com.sap.sailing.gwt.dispatch.client.StringsResult;
import com.sap.sailing.gwt.home.communication.search.GetSearchResultsAction;
import com.sap.sailing.gwt.home.communication.search.GetSearchServerNamesAction;
import com.sap.sailing.gwt.home.communication.search.SearchResultDTO;
import com.sap.sailing.gwt.home.shared.app.ActivityCallback;

public class SearchResultActivity extends AbstractActivity {
   
    private final SearchResultClientFactory clientFactory;
    private final SearchResultPlace searchResultPlace;

    public SearchResultActivity(SearchResultPlace place, SearchResultClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.searchResultPlace = place;
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        Window.setTitle(searchResultPlace.getTitle());
        final SearchResultView view = clientFactory.createSearchResultView();
        if (searchResultPlace.getSearchText() != null && !searchResultPlace.getSearchText().isEmpty()) {
            final String searchText = searchResultPlace.getSearchText();
            view.setSearchText(searchText);
            GetSearchServerNamesAction action = new GetSearchServerNamesAction();
            searchOnServer(panel, view, new GetSearchResultsAction(searchText));
            clientFactory.getDispatch().execute(action, new ActivityCallback<StringsResult>(clientFactory, panel) {
                @Override
                public void onSuccess(StringsResult result) {
                    for (String serverName : result.getValues()) {
                        searchOnServer(panel, view, new GetSearchResultsAction(searchText, serverName));
                    }
                }
            });
        }
        panel.setWidget(view);
    }
    
    private void searchOnServer(AcceptsOneWidget panel, final SearchResultView view, GetSearchResultsAction action) {
        clientFactory.getDispatch().execute(action, new ActivityCallback<ListResult<SearchResultDTO>>(clientFactory, panel) {
            @Override
            public void onSuccess(ListResult<SearchResultDTO> result) {
                view.updateSearchResult(searchResultPlace.getSearchText(), result.getValues());
            }
        });
    }
    
}
