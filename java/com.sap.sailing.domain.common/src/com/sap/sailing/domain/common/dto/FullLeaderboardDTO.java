package com.sap.sailing.domain.common.dto;

public class FullLeaderboardDTO implements IncrementalOrFullLeaderboardDTO {
    private static final long serialVersionUID = 8958803649569036100L;
    private LeaderboardDTO leaderboardDTO;
    
    FullLeaderboardDTO() {}
    
    public FullLeaderboardDTO(LeaderboardDTO leaderboardDTO) {
        this.leaderboardDTO = leaderboardDTO;
    }

    @Override
    public LeaderboardDTO getLeaderboardDTO(LeaderboardDTO previousVersion) {
        return leaderboardDTO;
    }

}
