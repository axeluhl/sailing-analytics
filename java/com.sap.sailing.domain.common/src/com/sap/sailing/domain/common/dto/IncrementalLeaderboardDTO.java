package com.sap.sailing.domain.common.dto;

import java.util.Date;

public class IncrementalLeaderboardDTO extends LeaderboardDTO implements Cloneable, IncrementalOrFullLeaderboardDTO {
    private static final long serialVersionUID = -7011986430671280594L;
    private LeaderboardDTO previousVersion;
    
    public IncrementalLeaderboardDTO(Date timePointOfLastCorrectionsValidity, String comment, boolean higherScoreIsBetter, UUIDGenerator uuidGenerator) {
        super(timePointOfLastCorrectionsValidity, comment, higherScoreIsBetter, uuidGenerator);
    }
    
    @Override
    public LeaderboardDTO getLeaderboardDTO(LeaderboardDTO previousVersion) {
        if (this.previousVersion != null && this.previousVersion != previousVersion) {
            throw new IllegalStateException("This incremental leaderboard DTO was already applied to a different previous version. It cannot be applied multiple times.");
        }
        if (previousVersion == null) {
            throw new IllegalArgumentException("Must provide a valid previous leaderboard version to reconstruct full LeaderboardDTO from differential leaderboard DTO");
        }
        applyThisToPreviousVersionByUpdatingThis(previousVersion);
        return this;
    }

    private void applyThisToPreviousVersionByUpdatingThis(LeaderboardDTO previousVersion) {
        if (this.previousVersion != null && this.previousVersion != previousVersion) {
            throw new IllegalStateException("This incremental leaderboard DTO was already applied to a different previous version. It cannot be applied multiple times.");
        }
        this.previousVersion = previousVersion;
    }
}
