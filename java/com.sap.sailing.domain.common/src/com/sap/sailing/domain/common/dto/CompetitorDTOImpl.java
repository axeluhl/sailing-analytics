package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

public class CompetitorDTOImpl extends NamedDTO implements CompetitorDTO, Serializable {
    private static final long serialVersionUID = -4997852354821083154L;
    private String twoLetterIsoCountryCode;
    private String threeLetterIocCountryCode;
    private String countryName;
    private String sailID;
    private String idAsString;
    private BoatClassDTO boatClass;
    
    CompetitorDTOImpl() {}
    
    public CompetitorDTOImpl(String name, String twoLetterIsoCountryCode, String threeLetterIocCountryCode,
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
        result = prime * getIdAsString().hashCode();
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
        CompetitorDTOImpl other = (CompetitorDTOImpl) obj;
        if (getIdAsString() == null) {
            if (other.getIdAsString() != null)
                return false;
        } else if (!getIdAsString().equals(other.getIdAsString()))
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

}
