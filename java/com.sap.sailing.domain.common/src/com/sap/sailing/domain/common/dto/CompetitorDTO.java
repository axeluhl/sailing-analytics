package com.sap.sailing.domain.common.dto;

public interface CompetitorDTO extends CompetitorWithoutBoatDTO {
    String getSailID();
    
    BoatClassDTO getBoatClass();

    BoatDTO getBoat();
    
    /**
     * A regular instance will simply return this object. A compacted version may compute the result by looking it up
     * from the previous version of the enclosing leaderboard.
     */
    CompetitorDTO getCompetitorFromPrevious(LeaderboardDTO previousVersion);
}
