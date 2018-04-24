package com.sap.sailing.gwt.common.communication.routing;

import com.google.gwt.http.client.URL;
import com.sap.sse.gwt.client.ServiceRoutingProvider;

/**
 * Interface used to provide leaderboard specific routing, see {@link ServiceRoutingProvider}
 */
public interface ProvidesLeaderboardRouting extends ServiceRoutingProvider {
    String LEADERBOARDNAME_PREFIX ="/leaderboard/";

    String  getLeaderboardname();
    
    default String routingSuffixPath() {
        return new StringBuilder()
                .append(LEADERBOARDNAME_PREFIX)
                .append(URL.encodePathSegment(getLeaderboardname()))
                .toString();
    }
}
