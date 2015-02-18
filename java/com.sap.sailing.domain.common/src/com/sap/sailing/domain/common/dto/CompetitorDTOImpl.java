package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

import com.sap.sse.common.Color;

/**
 * Equality and hash code are based on the {@link #getIdAsString() ID}, the {@link #getSailID() sail number}, the
 * {@link #getBoatClass() boat class} (whose equality and hash code, in turn, depends on its name) and the
 * {@link #getThreeLetterIocCountryCode() IOC country code}. Note that the three latter properties are subject
 * to change for a competitor while the ID remains unchanged.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class CompetitorDTOImpl extends NamedDTO implements CompetitorDTO, Serializable {
    private static final long serialVersionUID = -4997852354821083154L;
    private String countryName;
    private String twoLetterIsoCountryCode;
    private String threeLetterIocCountryCode;
    private Color color;
    private String email;
    private String sailID;
    private String idAsString;
    private BoatClassDTO boatClass;
    
    public CompetitorDTOImpl() {}
    
    public CompetitorDTOImpl(String name, Color color, String email, String twoLetterIsoCountryCode, String threeLetterIocCountryCode,
            String countryName, String sailID, String idAsString, BoatClassDTO boatClass) {
        super(name);
        this.color = color;
        this.email = email;
        this.twoLetterIsoCountryCode = twoLetterIsoCountryCode;
        this.threeLetterIocCountryCode = threeLetterIocCountryCode;
        this.countryName = countryName;
        this.sailID = sailID;
        this.idAsString = idAsString;
        this.boatClass = boatClass;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((boatClass == null) ? 0 : boatClass.hashCode());
        result = prime * result + ((idAsString == null) ? 0 : idAsString.hashCode());
        result = prime * result + ((sailID == null) ? 0 : sailID.hashCode());
        result = prime * result + ((color == null) ? 0 : color.hashCode());
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((threeLetterIocCountryCode == null) ? 0 : threeLetterIocCountryCode.hashCode());
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
        if (idAsString == null) {
            if (other.idAsString != null)
                return false;
        } else if (!idAsString.equals(other.idAsString))
            return false;
        if (sailID == null) {
            if (other.sailID != null)
                return false;
        } else if (!sailID.equals(other.sailID))
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
        return true;
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
    public String getSailID() {
        return sailID;
    }

    @Override
    public String getIdAsString() {
        return idAsString;
    }

    @Override
    public BoatClassDTO getBoatClass() {
        return boatClass;
    }

    @Override
    public CompetitorDTO getCompetitorFromPrevious(LeaderboardDTO previousVersion) {
        return this;
    }

    public Color getColor() {
        return color;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String email(){
        return email;
    }

    @Override
    public boolean hasEmail() {
        return email != null && !email.isEmpty();
    }
}
