package com.sap.sailing.domain.common.dto;

import java.util.Date;

public class FullLeaderboardDTO implements IncrementalOrFullLeaderboardDTO {
    private static final long serialVersionUID = 8958803649569036100L;
    private LeaderboardDTO leaderboardDTO;
    private Date currentServerTime;
    
    FullLeaderboardDTO() {}
    
    public FullLeaderboardDTO(LeaderboardDTO leaderboardDTO) {
        this.leaderboardDTO = leaderboardDTO;
        currentServerTime = new Date();
    }

    @Override
    public LeaderboardDTO getLeaderboardDTO(LeaderboardDTO previousVersion) {
        return leaderboardDTO;
    }

    @Override
    public Date getCurrentServerTime() {
        return currentServerTime;
    }

}
