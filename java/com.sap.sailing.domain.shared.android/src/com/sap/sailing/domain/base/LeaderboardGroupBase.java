package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Renamable;
import com.sap.sailing.domain.common.WithID;

public interface LeaderboardGroupBase extends Renamable, WithID {
    String getDescription();
    void setDescriptiom(String description);
    boolean hasOverallLeaderboard();
}
