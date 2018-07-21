package com.sap.sailing.domain.leaderboard.caching;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardCacheManager;

/**
 * Interface for leaderboard state cache management. When passed to a {@link LeaderboardCacheManager}, leaderboards
 * can be registered using {@link LeaderboardCacheManager#add(Leaderboard)} which will transitively call {@link #add(Leaderboard)}
 * on the leaderboard cache and start to observe the leaderboard for changes. When the leaderboard cache manager observes
 * changes, it calls {@link #invalidate(Leaderboard)} on this cache for the leaderboard affected by the changes.
 * 
 * This is a fairly coarse-grained way of cache invalidation because depending on what about the leaderboard is being
 * cached, the change may not affect the datum cached. Future versions of this may also carry through the particular
 * type of change so the cache can decide itself whether an invalidation / recalculation actually has to happen.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface LeaderboardCache {
    void add(Leaderboard leaderboard);
    void invalidate(Leaderboard leaderboard);
}
