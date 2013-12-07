package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

public interface CompetitorDTO extends Serializable {
    
    String getTwoLetterIsoCountryCode();

    String getThreeLetterIocCountryCode();

    String getCountryName();

    String getSailID();

    String getIdAsString();

    BoatClassDTO getBoatClass();
    
    String getName();
    
    String getColor();

    /**
     * A regular instance will simply return this object. A compacted version may compute the result by looking it up
     * from the previous version of the enclosing leaderboard.
     */
    CompetitorDTO getCompetitorFromPrevious(LeaderboardDTO previousVersion);
    
}
