package com.sap.sailing.server.gateway.interfaces;

import org.json.simple.JSONObject;

import com.sap.sse.common.Util;

/**
 * The result of {@link SailingServer#compareServers(java.util.Optional, SailingServer, java.util.Optional) asking} a
 * {@link SailingServer} for the leaderboard group content differences with another {@link SailingServer}. The differences
 * reported by {@link #getADiffs()} and {@link #getBDiffs()} are JSON representations
 * of a {@code LeaderboardGroup}, stripped down to the differences plus the leaderboard group identification fields,
 * particularly its ID. A leaderboard group is listed only if a difference was found. Conversely, the comparison
 * has not found any diffs if and only if both, {@link #getADiffs()} and {@link #getBDiffs()} are empty.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface CompareServersResult {
    /**
     * The hostname and optional port specification ("authority") of the first server
     */
    String getServerA();

    /**
     * The hostname and optional port specification ("authority") of the second server
     */
    String getServerB();

    Iterable<JSONObject> getADiffs();

    Iterable<JSONObject> getBDiffs();

    default boolean hasDiffs() {
        return !Util.isEmpty(getADiffs()) || !Util.isEmpty(getBDiffs());
    }
}
