package com.sap.sailing.gwt.home.communication.search;

import java.net.URL;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.LeaderboardSearchResultBase;
import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.common.Util;
import com.sap.sse.common.search.KeywordQuery;
import com.sap.sse.common.search.Result;
import com.sap.sse.gwt.dispatch.client.system.batching.NonBatchableAction;
import com.sap.sse.gwt.dispatch.shared.commands.ListResult;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * Use this action once for each result of {@link GetSearchServerNamesAction} and once for the main server.
 * 
 * @see {@link #GetSearchResultsAction(String)} to search on the server to which this request is sent
 * @see #GetSearchResultsAction(String, String) use a name as retrieved by {@link GetSearchServerNamesAction} which
 *      corresponds to a name of a {@link RemoteSailingServerReference}, to search a remote server.
 */
public class GetSearchResultsAction implements SailingAction<ListResult<SearchResultDTO>>, NonBatchableAction {

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
    public ListResult<SearchResultDTO> execute(SailingDispatchContext ctx) throws DispatchException {
        KeywordQuery searchQuery = new KeywordQuery(Util.splitAlongWhitespaceRespectingDoubleQuotedPhrases(searchText));
        RacingEventService service = ctx.getRacingEventService();
        final ListResult<SearchResultDTO> result;
        if (remoteServerName == null) {
            result = getListResult(service.search(searchQuery), ctx.getRequestBaseURL(), false);
        } else {
            RemoteSailingServerReference remoteServer = service.getRemoteServerReferenceByName(remoteServerName);
            result = getListResult(service.searchRemotely(remoteServerName, searchQuery), remoteServer.getURL(), true);
        }
        return result;
    }

    @GwtIncompatible
    private <T extends LeaderboardSearchResultBase> ListResult<SearchResultDTO> getListResult(Result<T> result,
            URL baseUrl, boolean isOnRemoteServer) {
        ListResult<SearchResultDTO> resultList = new ListResult<>();
        if (result != null) {
            for (T hit : result.getHits()) {
                // TODO: for now filter all results where we no event is defined
                if (hit.getEvents() != null && !Util.isEmpty(hit.getEvents())) {
                    resultList.addValue(new SearchResultDTO(hit, baseUrl, isOnRemoteServer));
                }
            }
        }
        return resultList;
    }

}
