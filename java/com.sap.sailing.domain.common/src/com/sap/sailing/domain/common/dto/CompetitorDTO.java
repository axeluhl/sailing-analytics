package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

import com.sap.sailing.domain.common.racelog.tracking.MappableToDevice;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;

/**
 * Equality and hash code are based on the {@link #getIdAsString() ID} and all contained attributes like name, shortName, email, etc.
 */
public interface CompetitorDTO extends Serializable, MappableToDevice {
    
    String getTwoLetterIsoCountryCode();

    String getThreeLetterIocCountryCode();

    String getCountryName();

    String getIdAsString();

    String getSearchTag();

    /**
     * If the {@code searchTag} is not contained in {@link #getSearchTag()}, appends it to the search tag, separated by a space character 
     */
    void addToSearchTag(String searchTag);
    
    String getName();

    String getShortName();

    /**
     * Returns a derived short information about a competitor depending on the information available. If we have a
     * {@link #getShortName() short name} set on the competitor this name will be returned. If no short name exist but a
     * {@link CompetitorWithBoatDTO#getBoat() boat} then either the sailId or the boat name will returned. If all these
     * attributes have no value, a three-letter acronym is constructed from the name by using the first two and the last letter
     * of the competitor's {@link #getName() name} unless it's empty in which case an empty string is returned.
     */
    String getShortInfo();

    Color getColor();
    
    String getEmail();
    
    boolean hasEmail();

    String getFlagImageURL();

    String getImageURL();

    Double getTimeOnTimeFactor();
    
    Duration getTimeOnDistanceAllowancePerNauticalMile();
    
    boolean hasBoat();


    /**
     * A regular instance will simply return this object. A compacted version may compute the result by looking it up
     * from the previous version of the enclosing leaderboard.
     */
    CompetitorDTO getCompetitorFromPrevious(LeaderboardDTO previousVersion);
}
