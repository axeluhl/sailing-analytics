package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

public class SimpleCompetitorDTO extends NamedDTO implements Serializable {

    private static final long serialVersionUID = -5743976446085202047L;
    
    private String sailID;
    private String twoLetterIsoCountryCode;
    private String flagImageURL;

    protected SimpleCompetitorDTO() {
    }

    public SimpleCompetitorDTO(String name, String sailID, String twoLetterIsoCountryCode, String flagImageURL) {
        super(name);
        this.sailID = sailID;
        this.twoLetterIsoCountryCode = twoLetterIsoCountryCode;
        this.flagImageURL = flagImageURL;
    }

    public String getSailID() {
        return sailID;
    }

    public void setSailID(String sailID) {
        this.sailID = sailID;
    }

    public String getTwoLetterIsoCountryCode() {
        return twoLetterIsoCountryCode;
    }

    public void setTwoLetterIsoCountryCode(String twoLetterIsoCountryCode) {
        this.twoLetterIsoCountryCode = twoLetterIsoCountryCode;
    }

    public String getFlagImageURL() {
        return flagImageURL;
    }

    public void setFlagImageURL(String flagImageURL) {
        this.flagImageURL = flagImageURL;
    }
    
}
