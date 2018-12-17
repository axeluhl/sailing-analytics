package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.dto.NamedSecuredObjectDTO;

/**
 * Equality and hash code are based on the {@link #getIdAsString() ID}, the {@link #getSailID() sail number}, the
 * {@link #getBoatClass() boat class} (whose equality and hash code, in turn, depends on its name) and the
 * {@link #getThreeLetterIocCountryCode() IOC country code}. Note that the three latter properties are subject
 * to change for a competitor while the ID remains unchanged.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class CompetitorDTOImpl extends NamedSecuredObjectDTO implements CompetitorDTO, Serializable {
    private static final long serialVersionUID = 3019126418065082622L;
    private String countryName;
    private String twoLetterIsoCountryCode;
    private String threeLetterIocCountryCode;
    private Color color;
    private String shortName;
    private String email;
    private String searchTag;
    private String idAsString;
    private String imageURL;
    private String flagImageURL;
    private Double timeOnTimeFactor;
    private Duration timeOnDistanceAllowancePerNauticalMile;
    
    public CompetitorDTOImpl() {}
    
    public CompetitorDTOImpl(String name, String shortName, Color color, String email, String twoLetterIsoCountryCode, String threeLetterIocCountryCode,
            String countryName, String idAsString, String imageURL, String flagImageURL, 
            Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String searchTag) {
        super(name);
        this.shortName = shortName;
        this.color = color;
        this.email = email;
        this.twoLetterIsoCountryCode = twoLetterIsoCountryCode;
        this.threeLetterIocCountryCode = threeLetterIocCountryCode;
        this.countryName = countryName;
        this.idAsString = idAsString;
        this.imageURL = imageURL;
        this.flagImageURL = flagImageURL;
        this.timeOnTimeFactor = timeOnTimeFactor;
        this.timeOnDistanceAllowancePerNauticalMile = timeOnDistanceAllowancePerNauticalMile;
        this.searchTag = searchTag;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((idAsString == null) ? 0 : idAsString.hashCode());
        result = prime * result + ((color == null) ? 0 : color.hashCode());
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((shortName == null) ? 0 : shortName.hashCode());
        result = prime * result + ((threeLetterIocCountryCode == null) ? 0 : threeLetterIocCountryCode.hashCode());
        result = prime * result + ((imageURL == null) ? 0 : imageURL.hashCode());
        result = prime * result + ((flagImageURL == null) ? 0 : flagImageURL.hashCode());
        result = prime * result + ((timeOnTimeFactor == null) ? 0 : timeOnTimeFactor.hashCode());
        result = prime * result + ((timeOnDistanceAllowancePerNauticalMile == null) ? 0 : timeOnDistanceAllowancePerNauticalMile.hashCode());
        result = prime * result + ((searchTag == null) ? 0 : searchTag.hashCode());
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
        if (idAsString == null) {
            if (other.idAsString != null)
                return false;
        } else if (!idAsString.equals(other.idAsString))
            return false;
        if (threeLetterIocCountryCode == null) {
            if (other.threeLetterIocCountryCode != null)
                return false;
        } else if (!threeLetterIocCountryCode.equals(other.threeLetterIocCountryCode))
            return false;
        if (color == null) {
            if (other.color != null)
                return false;
        } else if (!color.equals(other.color))
            return false;
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        if (shortName == null) {
            if (other.shortName != null)
                return false;
        } else if (!shortName.equals(other.shortName))
            return false;
        if (imageURL == null) {
            if (other.imageURL != null)
                return false;
        } else if (!imageURL.equals(other.imageURL))
            return false;
        if (flagImageURL == null) {
            if (other.flagImageURL != null)
                return false;
        } else if (!flagImageURL.equals(other.flagImageURL))
            return false;
        if (timeOnTimeFactor == null) {
            if (other.timeOnTimeFactor != null)
                return false;
        } else if (!timeOnTimeFactor.equals(other.timeOnTimeFactor))
            return false;
        if (timeOnDistanceAllowancePerNauticalMile == null) {
            if (other.timeOnDistanceAllowancePerNauticalMile != null)
                return false;
        } else if (!timeOnDistanceAllowancePerNauticalMile.equals(other.timeOnDistanceAllowancePerNauticalMile))
            return false;
        if (searchTag == null) {
            if (other.searchTag != null)
                return false;
        } else if (!searchTag.equals(other.searchTag))
            return false;
        return true;
    }

    @Override
    public CompetitorDTO getCompetitorFromPrevious(LeaderboardDTO previousVersion) {
        return this;
    }

    @Override
    public String getTwoLetterIsoCountryCode() {
        return twoLetterIsoCountryCode;
    }

    @Override
    public String getThreeLetterIocCountryCode() {
        return threeLetterIocCountryCode;
    }

    @Override
    public String getCountryName() {
        return countryName;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public String getShortInfo() {
        final String result;
        if (getShortName() != null && !getShortName().trim().isEmpty()) {
            result = getShortName(); 
        } else {
            final String trimmedName = getName().trim();
            if (trimmedName.isEmpty()) {
                result = null;
            } else {
                result = (trimmedName.length()>0?""+trimmedName.charAt(0):"")
                    + (trimmedName.length()>1?trimmedName.charAt(1):"")
                    + (trimmedName.length()>0?trimmedName.charAt(trimmedName.length()-1):"");
                        
            }
        }
        return result;
    }

    @Override
    public String getImageURL() {
        return imageURL;
    }

    @Override
    public String getFlagImageURL() {
        return flagImageURL;
    }

    @Override
    public String getIdAsString() {
        return idAsString;
    }

    @Override
    public Serializable getId() {
        return idAsString;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public String getSearchTag() {
        return searchTag;
    }
    
    @Override
    public void addToSearchTag(String searchTag) {
        if (this.searchTag == null) {
            this.searchTag = searchTag;
        } else if (!this.searchTag.contains(searchTag)) {
            this.searchTag += " "+searchTag;
        }
    }

    @Override
    public String getEmail() {
        return email;
    }
    
    @Override
    public boolean hasEmail() {
        return email != null && !email.isEmpty();
    }

    @Override
    public Double getTimeOnTimeFactor() {
        return timeOnTimeFactor;
    }

    @Override
    public Duration getTimeOnDistanceAllowancePerNauticalMile() {
        return timeOnDistanceAllowancePerNauticalMile;
    }

    @Override
    public boolean hasBoat() {
        return false;
    }

    @Override
    public TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(String... params) {
        return new TypeRelativeObjectIdentifier(idAsString);
    }

    @Override
    public HasPermissions getType() {
        return SecuredDomainType.COMPETITOR;
    }

}
