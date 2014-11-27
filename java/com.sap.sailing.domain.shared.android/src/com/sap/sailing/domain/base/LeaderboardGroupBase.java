package com.sap.sailing.domain.base;

import java.util.UUID;

import com.sap.sailing.domain.common.Renamable;
import com.sap.sse.common.WithID;

public interface LeaderboardGroupBase extends Renamable, WithID {
    UUID getId();
    String getDescription();
    void setDescriptiom(String description);
    boolean hasOverallLeaderboard();
    String getDisplayName();
    void setDisplayName(String displayName);
}
