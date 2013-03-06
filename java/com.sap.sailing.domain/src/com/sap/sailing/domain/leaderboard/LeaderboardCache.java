package com.sap.sailing.domain.leaderboard;

/**
 * Interface for leaderboard state cache management. When produced and managed by a {@link LeaderboardCacheManager}, the cache
 * will 
 * @author Axel Uhl (D043530)
 *
 */
public interface LeaderboardCache {
    void add(Leaderboard leaderboard);
    void removeFromCache(Leaderboard leaderboard);
}
