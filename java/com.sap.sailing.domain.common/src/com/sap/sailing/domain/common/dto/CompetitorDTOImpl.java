package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;

public class CompetitorDTOImpl extends CompetitorWithoutBoatDTOImpl implements CompetitorDTO, Serializable {
    private static final long serialVersionUID = 6749455739529431935L;
    private BoatClassDTO boatClass;
    private BoatDTO boat;
    
    public CompetitorDTOImpl() {}
    
    public CompetitorDTOImpl(String name, String shortName, Color color, String email, String twoLetterIsoCountryCode, String threeLetterIocCountryCode,
            String countryName, String idAsString, String imageURL, String flagImageURL, 
            BoatDTO boat, BoatClassDTO boatClass, Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String searchTag) {
        super(name, shortName, color, email, twoLetterIsoCountryCode, threeLetterIocCountryCode,
                countryName, idAsString, imageURL, flagImageURL, 
                timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag);
        this.boat = boat;
        this.boatClass = boatClass;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((boatClass == null) ? 0 : boatClass.hashCode());
        result = prime * result + ((boat == null) ? 0 : boat.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CompetitorDTOImpl other = (CompetitorDTOImpl) obj;
        if (boatClass == null) {
            if (other.boatClass != null)
                return false;
        } else if (!boatClass.equals(other.boatClass))
            return false;
        if (boat == null) {
            if (other.boat != null)
                return false;
        } else if (!boat.equals(other.boat))
            return false;
        return true;
    }

    @Override
    public CompetitorDTO getCompetitorFromPrevious(LeaderboardDTO previousVersion) {
        return this;
    }

    @Override
    public String getSailID() {
        return boat==null?null:boat.getSailId();
    }
    
    @Override
    public BoatClassDTO getBoatClass() {
        return boatClass;
    }

    @Override
    public BoatDTO getBoat() {
        return boat;
    }
}
