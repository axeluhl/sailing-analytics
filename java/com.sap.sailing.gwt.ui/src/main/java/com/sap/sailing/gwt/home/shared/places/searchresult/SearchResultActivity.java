package com.sap.sailing.gwt.home.shared.places.searchresult;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.communication.search.GetSearchResultsAction;
import com.sap.sailing.gwt.home.communication.search.GetSearchServerNamesAction;
import com.sap.sailing.gwt.home.communication.search.SearchResultDTO;
import com.sap.sailing.gwt.home.shared.app.ActivityCallback;
import com.sap.sse.gwt.dispatch.shared.commands.ListResult;
import com.sap.sse.gwt.dispatch.shared.commands.StringsResult;

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
            view.setBusy(true);
            final AtomicInteger numberOfOutstandingRequests = new AtomicInteger(1);
            searchOnServer(panel, view, new GetSearchResultsAction(searchText), numberOfOutstandingRequests);
            clientFactory.getDispatch().execute(new GetSearchServerNamesAction(),
                    new ActivityCallback<StringsResult>(clientFactory, panel) {
                @Override
                public void onSuccess(StringsResult result) {
                    for (String serverName : result.getValues()) {
                        numberOfOutstandingRequests.incrementAndGet();
                        searchOnServer(panel, view, new GetSearchResultsAction(searchText, serverName), numberOfOutstandingRequests);
                    }
                }
            });
        }
        panel.setWidget(view);
    }
    
    private void searchOnServer(AcceptsOneWidget panel, final SearchResultView view, GetSearchResultsAction action, final AtomicInteger numberOfOutstandingRequests) {
        clientFactory.getDispatch().execute(action, new ActivityCallback<ListResult<SearchResultDTO>>(clientFactory, panel) {
            @Override
            public void onSuccess(ListResult<SearchResultDTO> result) {
                view.updateSearchResult(searchResultPlace.getSearchText(), result.getValues());
                manageBusyState(view, numberOfOutstandingRequests);
            }

            private void manageBusyState(final SearchResultView view, AtomicInteger numberOfOutstandingRequests) {
                if (numberOfOutstandingRequests.decrementAndGet() <= 0) {
                    view.setBusy(false);
                }
            }
            
            @Override
            public void onFailure(Throwable caught) {
                super.onFailure(caught);
                manageBusyState(view, numberOfOutstandingRequests);
            }
        });
    }
    
}
