package com.sap.sailing.gwt.home.client.place.searchresult;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.shared.placeholder.Placeholder;
import com.sap.sailing.gwt.ui.shared.LeaderboardSearchResultDTO;
import com.sap.sse.common.search.KeywordQuery;
import com.sap.sse.gwt.client.mvp.ErrorView;

public class SearchResultActivity extends AbstractActivity {
    private final SearchResultClientFactory clientFactory;

    private final SearchResultPlace searchResultPlace;

    private SearchResultView view;
    private AcceptsOneWidget panel;
    
    public SearchResultActivity(SearchResultPlace place, SearchResultClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.searchResultPlace = place;
        
        bindEvents();
    }

    private void bindEvents() {
        clientFactory.getEventBus().addHandler(SearchEvent.TYPE, new SearchEventHandler() {
            public void onDoSearch(SearchEvent event) {
                if(view != null) {
                    doSearch(event.getSearchText());
                }
            }
        });
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        this.panel = panel;
        panel.setWidget(new Placeholder());

        view = clientFactory.createSearchResultView();
        panel.setWidget(view.asWidget());
        doSearch(searchResultPlace.getSearchText());
    }

    protected void doSearch(final String searchText) {
        KeywordQuery searchQuery = new KeywordQuery(searchText);
        clientFactory.getSailingService().search(null, searchQuery, new AsyncCallback<Iterable<LeaderboardSearchResultDTO>>() {
            @Override
            public void onSuccess(Iterable<LeaderboardSearchResultDTO> searchResults) {
                view.updateSearchResult(searchText, searchResults);
            }

            @Override
            public void onFailure(Throwable caught) {
                final ErrorView view = clientFactory.createErrorView("Error while seaching with service search()", caught);
                panel.setWidget(view.asWidget());
            }
        });
    }

}
