package com.sap.sailing.gwt.ui.shared.dispatch.search;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.LeaderboardSearchResult;
import com.sap.sailing.domain.base.LeaderboardSearchResultBase;
import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ListResult;
import com.sap.sse.common.search.KeywordQuery;

public class GetSearchResultsAction implements Action<ListResult<SearchResultDTO>> {
    
    private String searchText;
    
    protected GetSearchResultsAction() {
    }
    
    public GetSearchResultsAction(String searchText) {
        this.searchText = searchText;
    }

    @Override
    @GwtIncompatible
    public ListResult<SearchResultDTO> execute(DispatchContext ctx) throws Exception {
        KeywordQuery searchQuery = new KeywordQuery(searchText.split("[ \t]+"));
        ListResult<SearchResultDTO> result = new ListResult<>();
        for (LeaderboardSearchResult hit : ctx.getRacingEventService().search(searchQuery).getHits()) {
            result.addValue(new SearchResultDTO(hit, ctx.getRequestBaseURL(), false));
        }
        for (RemoteSailingServerReference remoteServerRef : ctx.getRacingEventService().getLiveRemoteServerReferences()) {
            for (LeaderboardSearchResultBase hit : ctx.getRacingEventService().searchRemotely(remoteServerRef.getName(), searchQuery).getHits()) {
                result.addValue(new SearchResultDTO(hit, remoteServerRef.getURL(), true));
            }
        }
        return result;
    }
    
}
