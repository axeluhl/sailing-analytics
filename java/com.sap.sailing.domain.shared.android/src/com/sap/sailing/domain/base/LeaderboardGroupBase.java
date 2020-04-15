package com.sap.sailing.domain.base;

import java.util.UUID;

import com.sap.sse.common.NamedWithID;
import com.sap.sse.common.Renamable;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

public interface LeaderboardGroupBase extends Renamable, NamedWithID, WithQualifiedObjectIdentifier {
    UUID getId();
    String getDescription();
    void setDescriptiom(String description);
    boolean hasOverallLeaderboard();
    String getDisplayName();
    void setDisplayName(String displayName);
}
