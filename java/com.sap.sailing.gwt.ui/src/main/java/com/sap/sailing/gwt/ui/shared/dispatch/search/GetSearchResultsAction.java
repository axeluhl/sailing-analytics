package com.sap.sailing.gwt.ui.shared.dispatch.search;

import java.net.URL;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.LeaderboardSearchResultBase;
import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ListResult;
import com.sap.sailing.gwt.ui.shared.dispatch.NonBatchableAction;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.search.KeywordQuery;
import com.sap.sse.common.search.Result;

/**
 * Use this action once for each result of {@link GetSearchServerNamesAction} and once for the main server.
 * 
 * @see {@link #GetSearchResultsAction(String)} to search on the server to which this request is sent
 * @see #GetSearchResultsAction(String, String) use a name as retrieved by {@link GetSearchServerNamesAction} which
 *      corresponds to a name of a {@link RemoteSailingServerReference}, to search a remote server.
 */
public class GetSearchResultsAction implements Action<ListResult<SearchResultDTO>>, NonBatchableAction {

    private String searchText;
    private String remoteServerName;

    @SuppressWarnings("unused")
    private GetSearchResultsAction() {
    }

    public GetSearchResultsAction(String searchText) {
        this(searchText, null);
    }

    public GetSearchResultsAction(String searchText, String remoteServerName) {
        this.searchText = searchText;
        this.remoteServerName = remoteServerName;
    }

    @Override
    @GwtIncompatible
    public ListResult<SearchResultDTO> execute(DispatchContext ctx) throws Exception {
        KeywordQuery searchQuery = new KeywordQuery(searchText.split("[ \t]+"));
        RacingEventService service = ctx.getRacingEventService();
        if (remoteServerName == null) {
            return getListResult(service.search(searchQuery), ctx.getRequestBaseURL(), false);
        }
        RemoteSailingServerReference remoteServer = service.getRemoteServerReferenceByName(remoteServerName);
        return getListResult(service.searchRemotely(remoteServerName, searchQuery), remoteServer.getURL(), true);
    }

    @GwtIncompatible
    private <T extends LeaderboardSearchResultBase> ListResult<SearchResultDTO> getListResult(Result<T> result,
            URL baseUrl, boolean isOnRemoteServer) {
        ListResult<SearchResultDTO> resultList = new ListResult<>();
        for (T hit : result.getHits()) {
            // TODO: for now filter all results where we no event is defined
            if (hit.getEvent() != null) {
                resultList.addValue(new SearchResultDTO(hit, baseUrl, isOnRemoteServer));
            }
        }
        return resultList;
    }

}
