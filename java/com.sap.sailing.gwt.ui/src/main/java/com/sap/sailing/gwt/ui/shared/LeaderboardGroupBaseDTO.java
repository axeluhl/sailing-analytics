package com.sap.sailing.gwt.ui.shared;

import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.WithID;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.dto.NamedSecuredObjectDTO;

public class LeaderboardGroupBaseDTO extends NamedSecuredObjectDTO implements WithID, IsSerializable {
    
    private static final long serialVersionUID = -4276452763988957L;
    private UUID id;
    public String description;
    private String displayName;
    private boolean hasOverallLeaderboard;

    protected LeaderboardGroupBaseDTO() {} // for deserialization
    
    public LeaderboardGroupBaseDTO(UUID id, String name, String displayName) {
        super(name);
        this.id = id;
        this.displayName = displayName;
    }

    public LeaderboardGroupBaseDTO(UUID id, String name, String description, String displayName, boolean hasOverallLeaderboard) {
        this(id, name, displayName);
        this.description = description;
        this.hasOverallLeaderboard = hasOverallLeaderboard;
    }

    public UUID getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isHasOverallLeaderboard() {
        return hasOverallLeaderboard;
    }

    public void setHasOverallLeaderboard(boolean hasOverallLeaderboard) {
        this.hasOverallLeaderboard = hasOverallLeaderboard;
    }

    @Override
    public TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return new TypeRelativeObjectIdentifier(id.toString());
    }

}
