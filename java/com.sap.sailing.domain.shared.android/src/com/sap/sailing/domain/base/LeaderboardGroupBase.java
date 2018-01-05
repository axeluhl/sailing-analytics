package com.sap.sailing.domain.base;

import java.util.UUID;

import com.sap.sse.common.NamedWithID;
import com.sap.sse.common.Renamable;

public interface LeaderboardGroupBase extends Renamable, NamedWithID {
    UUID getId();
    String getDescription();
    void setDescriptiom(String description);
    boolean hasOverallLeaderboard();
    String getDisplayName();
    void setDisplayName(String displayName);
}
