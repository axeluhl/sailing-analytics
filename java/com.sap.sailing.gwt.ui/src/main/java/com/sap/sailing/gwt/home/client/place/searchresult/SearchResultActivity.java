package com.sap.sailing.gwt.home.client.place.searchresult;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.shared.LeaderboardSearchResultDTO;
import com.sap.sse.common.search.KeywordQuery;

public class SearchResultActivity extends AbstractActivity {
    private final SearchResultClientFactory clientFactory;

    private final SearchResultPlace searchResultPlace;

    public SearchResultActivity(SearchResultPlace place, SearchResultClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.searchResultPlace = place;
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        String queryText = searchResultPlace.getSearchQuery();
        KeywordQuery query = new KeywordQuery(queryText);
        clientFactory.getSailingService().search(null, query, new AsyncCallback<Iterable<LeaderboardSearchResultDTO>>() {
            @Override
            public void onSuccess(Iterable<LeaderboardSearchResultDTO> searchResult) {
                final SearchResultView view = clientFactory.createSearchResultView();
                panel.setWidget(view.asWidget());
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Shit happens");
            }
        });
    }

}
