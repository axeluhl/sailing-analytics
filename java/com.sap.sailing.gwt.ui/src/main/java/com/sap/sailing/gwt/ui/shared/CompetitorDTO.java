package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CompetitorDTO extends NamedDTO implements IsSerializable {
    public String twoLetterIsoCountryCode;
    public String threeLetterIocCountryCode;
    public String countryName;
    public String sailID;
    public String id;
    public BoatClassDTO boatClass;
    
    CompetitorDTO() {}
    
    public CompetitorDTO(String name, String twoLetterIsoCountryCode, String threeLetterIocCountryCode,
            String countryName, String sailID, String id, BoatClassDTO boatClass) {
        super(name);
        this.twoLetterIsoCountryCode = twoLetterIsoCountryCode;
        this.threeLetterIocCountryCode = threeLetterIocCountryCode;
        this.countryName = countryName;
        this.sailID = sailID;
        this.id = id;
        this.boatClass = boatClass;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * id.hashCode();
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
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
    
}
