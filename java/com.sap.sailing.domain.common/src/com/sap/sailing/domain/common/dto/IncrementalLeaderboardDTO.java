package com.sap.sailing.domain.common.dto;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IncrementalLeaderboardDTO extends LeaderboardDTO implements Cloneable, IncrementalOrFullLeaderboardDTO {
    private static final long serialVersionUID = -7011986430671280594L;
    private static final Logger logger = Logger.getLogger(IncrementalLeaderboardDTO.class.getName());
    
    public IncrementalLeaderboardDTO(Date timePointOfLastCorrectionsValidity, String comment, boolean higherScoreIsBetter) {
        super(timePointOfLastCorrectionsValidity, comment, higherScoreIsBetter);
    }
    
    @Override
    public LeaderboardDTO getLeaderboardDTO(LeaderboardDTO previousVersion) {
        if (previousVersion == null) {
            throw new IllegalArgumentException("Must provide a valid previous leaderboard version to reconstruct full LeaderboardDTO from differential leaderboard DTO");
        }
        try {
            IncrementalLeaderboardDTO result = (IncrementalLeaderboardDTO) clone();
            // TODO for all fields that are marked as "equal to previous" populate from previousVersion
            return result;
        } catch (CloneNotSupportedException e) {
            // internal error
            logger.log(Level.SEVERE, "Internal error. Clone on leaderboard DTO not supported", e);
            throw new RuntimeException(e);
        }
    }

}
