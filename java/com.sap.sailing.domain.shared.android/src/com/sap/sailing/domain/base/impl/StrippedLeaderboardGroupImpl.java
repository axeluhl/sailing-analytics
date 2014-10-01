package com.sap.sailing.domain.base.impl;

import java.util.UUID;

public class StrippedLeaderboardGroupImpl extends LeaderboardGroupBaseImpl {
    private static final long serialVersionUID = 7263952130620919924L;
    private final boolean hasOverallLeaderboard;

    public StrippedLeaderboardGroupImpl(UUID id, String name, String description, String displayName, boolean hasOverallLeaderboard) {
        super(id, name, description, displayName);
        this.hasOverallLeaderboard = hasOverallLeaderboard;
    }

    @Override
    public boolean hasOverallLeaderboard() {
        return hasOverallLeaderboard;
    }
}
