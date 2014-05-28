package com.sap.sailing.domain.leaderboard;

public interface LeaderboardGroupResolver {
    /**
     * @param leaderboardGroupName
     *            The name of the requested leaderboard group
     * @return The leaderboard group with the name <code>leaderboardGroupName</code>, or <code>null</code> if there is
     *         no such group within this resolver
     */
    LeaderboardGroup getLeaderboardGroupByName(String leaderboardGroupName);
}
