package com.sap.sailing.gwt.home.communication.search;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.gwt.dispatch.client.StringsResult;
import com.sap.sailing.gwt.dispatch.client.exceptions.DispatchException;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;

/**
 * A client should search a server in a two-step process. First, the client should ask the server which other servers
 * are available for searching additional content. Then, in a second step, the client should fire the queries by
 * parallel asynchronous action executions to the one server, passing the name of the remote server reference to search,
 * or <code>null</code> in order to search the server to which the query is executed. This allows a client to
 * asynchronously receive the results from various servers, not requiring the client to block until all results from all
 * servers have been received. The key reason for this two-step process is that the GWT RPC does not support streaming
 * of results.
 * 
 * This action load the list of server reference names, corresponding with {@link RemoteSailingServerReference#getName()},
 * to be used as parameter in {@link GetSearchResultsAction}. This list does <em>not</em> contain the <code>null</code>
 * value used to represent the search on the main server to which the query is sent.
 * 
 * @see GetSearchResultsAction#GetSearchResultsAction(String, String)
 */
public class GetSearchServerNamesAction implements SailingAction<StringsResult> {

    @Override
    @GwtIncompatible
    public StringsResult execute(SailingDispatchContext ctx) throws DispatchException {
        StringsResult result = new StringsResult();
        for (RemoteSailingServerReference remoteServerRef : ctx.getRacingEventService().getLiveRemoteServerReferences()) {
            result.addValue(remoteServerRef.getName());
        }
        return result;
    }
    
}
