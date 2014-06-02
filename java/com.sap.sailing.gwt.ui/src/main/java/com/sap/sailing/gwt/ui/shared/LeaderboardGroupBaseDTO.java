package com.sap.sailing.gwt.ui.shared;

import java.util.UUID;

import com.sap.sailing.domain.common.dto.NamedDTO;

public class LeaderboardGroupBaseDTO extends NamedDTO {
    private static final long serialVersionUID = -9023865026348923430L;
    private UUID id;
    public String description;
    private boolean hasOverallLeaderboard;

    LeaderboardGroupBaseDTO() {} // for deserialization
    
    public LeaderboardGroupBaseDTO(UUID id, String name) {
        super(name);
        this.id = id;
    }

    public LeaderboardGroupBaseDTO(UUID id, String name, String description, boolean hasOverallLeaderboard) {
        this(id, name);
        this.description = description;
        this.hasOverallLeaderboard = hasOverallLeaderboard;
    }

    public UUID getId() {
        return id;
    }

    public boolean isHasOverallLeaderboard() {
        return hasOverallLeaderboard;
    }

    public void setHasOverallLeaderboard(boolean hasOverallLeaderboard) {
        this.hasOverallLeaderboard = hasOverallLeaderboard;
    }
}
