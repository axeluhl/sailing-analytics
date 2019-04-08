package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;

public class CompetitorWithBoatDTOImpl extends CompetitorDTOImpl implements CompetitorWithBoatDTO, Serializable {
    private static final long serialVersionUID = -4997852354821083154L;
    private BoatDTO boat;
    
    public CompetitorWithBoatDTOImpl() {
        boat = new BoatDTO();
    }
    
    public CompetitorWithBoatDTOImpl(String name, String shortName, Color color, String email, String twoLetterIsoCountryCode, String threeLetterIocCountryCode,
            String countryName, String idAsString, String imageURL, String flagImageURL, 
            Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String searchTag, BoatDTO boat) {
        super(name, shortName, color, email, twoLetterIsoCountryCode, threeLetterIocCountryCode,
                countryName, idAsString, imageURL, flagImageURL, 
                timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag);
        this.boat = boat;
    }

    public CompetitorWithBoatDTOImpl(CompetitorDTO c, BoatDTO boat) {
        super(c.getName(), c.getShortName(), c.getColor(), c.getEmail(),
                c.getTwoLetterIsoCountryCode(), c.getThreeLetterIocCountryCode(), c.getCountryName(), c.getIdAsString(),
                c.getImageURL(), c.getFlagImageURL(), c.getTimeOnTimeFactor(),
                c.getTimeOnDistanceAllowancePerNauticalMile(), c.getSearchTag());
        this.boat = boat;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
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
        CompetitorWithBoatDTOImpl other = (CompetitorWithBoatDTOImpl) obj;
        if (boat == null) {
            if (other.boat != null)
                return false;
        } else if (!boat.equals(other.boat))
            return false;
        return true;
    }

    @Override
    public CompetitorWithBoatDTO getCompetitorFromPrevious(LeaderboardDTO previousVersion) {
        return this;
    }

    @Override
    public String getShortInfo() {
        final String result;
        if (getShortName() != null && !getShortName().trim().isEmpty()) {
            result = getShortName(); 
        } else if (getBoat() != null) {
            result = getBoat().getSailId() != null ? getBoat().getSailId() : getBoat().getName();
        } else {
            result = super.getShortName();
        }
        return result;
    }

    @Override
    public String getSailID() {
        return boat == null ? null : boat.getSailId();
    }
    
    @Override
    public BoatClassDTO getBoatClass() {
        return boat == null ? null : boat.getBoatClass();
    }

    @Override
    public void setBoat(BoatDTO boat) {
        this.boat = boat;
    }    

    @Override
    public BoatDTO getBoat() {
        return boat;
    }    

    @Override
    public boolean hasBoat() {
        return getBoat() != null;
    }
}
