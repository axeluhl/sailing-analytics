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

    Color getColor();
    
    String getEmail();
    
    boolean hasEmail();

    String getFlagImageURL();

    String getImageURL();

    Double getTimeOnTimeFactor();
    
    Duration getTimeOnDistanceAllowancePerNauticalMile();
    
    boolean hasBoat();

}
