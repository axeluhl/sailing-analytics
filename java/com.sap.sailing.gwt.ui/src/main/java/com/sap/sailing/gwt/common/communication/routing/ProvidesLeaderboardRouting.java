package com.sap.sailing.gwt.common.communication.routing;

import com.sap.sailing.domain.common.sharding.ShardingType;
import com.sap.sse.gwt.client.ServiceRoutingProvider;

/**
 * Interface used to provide leaderboard specific routing, see {@link ServiceRoutingProvider}
 */
public interface ProvidesLeaderboardRouting extends ServiceRoutingProvider {
    
    String getLeaderboardName();
    
    default String routingSuffixPath() {
        return ShardingType.LEADERBOARDNAME.encodeIfNeeded(getLeaderboardName());
    }
}
