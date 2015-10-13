package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.io.Serializable;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.NamedDTO;
import com.sap.sse.common.CountryCode;

public class SimpleCompetitorDTO extends NamedDTO implements Serializable {

    private static final long serialVersionUID = -5743976446085202047L;
    
    private String sailID;
    private String twoLetterIsoCountryCode;
    private String flagImageURL;

    protected SimpleCompetitorDTO() {
    }
    
    @GwtIncompatible
    public SimpleCompetitorDTO(Competitor competitor) {
        super(competitor.getName());
        final Nationality nationality = competitor.getTeam().getNationality();
        CountryCode countryCode = nationality == null ? null : nationality.getCountryCode();
        this.sailID = competitor.getBoat().getSailID();
        this.twoLetterIsoCountryCode = countryCode == null ? null : countryCode.getTwoLetterISOCode();
        this.flagImageURL = competitor.getFlagImage() == null ? null : competitor.getFlagImage().toString();
    }

    @GwtIncompatible
    public SimpleCompetitorDTO(CompetitorDTO competitor) {
        super(competitor.getName());
        this.sailID = competitor.getSailID();
        this.twoLetterIsoCountryCode = competitor.getTwoLetterIsoCountryCode();
        this.flagImageURL = competitor.getFlagImageURL();
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((sailID == null) ? 0 : sailID.hashCode());
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
        SimpleCompetitorDTO other = (SimpleCompetitorDTO) obj;
        if (sailID == null) {
            if (other.sailID != null)
                return false;
        } else if (!sailID.equals(other.sailID))
            return false;
        return true;
    }
    
}
