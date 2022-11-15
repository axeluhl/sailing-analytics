package com.sap.sailing.gwt.home.communication.search;

import java.net.URL;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.LeaderboardSearchResultBase;
import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.server.interfaces.KeywordQueryWithOptionalEventQualification;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.common.Util;
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
        final KeywordQueryWithOptionalEventQualification searchQuery;
        RacingEventService service = ctx.getRacingEventService();
        final ListResult<SearchResultDTO> result;
        final Iterable<String> splitAlongWhitespaceRespectingDoubleQuotedPhrases = Util.splitAlongWhitespaceRespectingDoubleQuotedPhrases(searchText);
        if (remoteServerName == null) {
            searchQuery = new KeywordQueryWithOptionalEventQualification(splitAlongWhitespaceRespectingDoubleQuotedPhrases);
            result = getListResult(service.search(searchQuery), ctx.getRequestBaseURL(), null, null, false);
        } else {
            RemoteSailingServerReference remoteServer = service.getRemoteServerReferenceByName(remoteServerName);
            // constrain the search with an optional event ID include/exclude list as specified by the remote server ref:
            searchQuery = new KeywordQueryWithOptionalEventQualification(
                    splitAlongWhitespaceRespectingDoubleQuotedPhrases, remoteServer.isInclude(),
                    remoteServer.getSelectedEventIds());
            result = getListResult(service
                    .searchRemotely(remoteServerName, searchQuery), remoteServer.getURL(),
                    remoteServer.isInclude(), remoteServer.getSelectedEventIds(), true);
        }
        return result;
    }

    /**
     * @param include
     *            passed through to {@link SearchResultDTO} constructor, filtering the event IDs reported by
     *            {@link SearchResultDTO#getEvents()}.
     * @param eventIds
     *            passed through to {@link SearchResultDTO} constructor, filtering the event IDs reported by
     *            {@link SearchResultDTO#getEvents()}.
     * @param isOnRemoteServer
     * @return
     */
    @GwtIncompatible
    private <T extends LeaderboardSearchResultBase> ListResult<SearchResultDTO> getListResult(Result<T> result,
            URL baseUrl, Boolean include, Set<UUID> eventIds, boolean isOnRemoteServer) {
        ListResult<SearchResultDTO> resultList = new ListResult<>();
        if (result != null) {
            for (T hit : result.getHits()) {
                if (hit.getEvents() != null && !Util.isEmpty(hit.getEvents())) {
                    resultList.addValue(new SearchResultDTO(hit, baseUrl, isOnRemoteServer, include, eventIds));
                }
            }
        }
        return resultList;
    }

}
