package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

import com.sap.sailing.domain.common.racelog.tracking.MappableToDevice;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;

public interface CompetitorDTO extends Serializable, MappableToDevice {
    
    String getTwoLetterIsoCountryCode();

    String getThreeLetterIocCountryCode();

    String getCountryName();

    String getIdAsString();

    String getSailID();

    String getSearchTag();

    /**
     * If the {@code searchTag} is not {@link String#contains(CharSequence) contained} in {@link #getSearchTag()},
     * appends it to the search tag, separated by a space character
     */
    void addToSearchTag(String searchTag);
    
    BoatDTO getBoat();

    BoatClassDTO getBoatClass();
    
    String getName();
    
    Color getColor();
    
    String getEmail();
    
    boolean hasEmail();

    /**
     * A regular instance will simply return this object. A compacted version may compute the result by looking it up
     * from the previous version of the enclosing leaderboard.
     */
    CompetitorDTO getCompetitorFromPrevious(LeaderboardDTO previousVersion);

    String getFlagImageURL();

    String getImageURL();

    Double getTimeOnTimeFactor();
    
    Duration getTimeOnDistanceAllowancePerNauticalMile();

}
