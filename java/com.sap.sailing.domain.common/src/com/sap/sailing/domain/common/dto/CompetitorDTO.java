package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

public class CompetitorDTO extends NamedDTO implements Serializable {
    private static final long serialVersionUID = -4997852354821083154L;
    public String twoLetterIsoCountryCode;
    public String threeLetterIocCountryCode;
    public String countryName;
    public String sailID;
    public String idAsString;
    public BoatClassDTO boatClass;
    
    CompetitorDTO() {}
    
    public CompetitorDTO(String name, String twoLetterIsoCountryCode, String threeLetterIocCountryCode,
            String countryName, String sailID, String idAsString, BoatClassDTO boatClass) {
        super(name);
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
        int result = 1;
        result = prime * idAsString.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CompetitorDTO other = (CompetitorDTO) obj;
        if (idAsString == null) {
            if (other.idAsString != null)
                return false;
        } else if (!idAsString.equals(other.idAsString))
            return false;
        return true;
    }
    
}
